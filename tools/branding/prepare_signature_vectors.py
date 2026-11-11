#!/usr/bin/env python3
"""Trace the approved Signature reference to Android vectors, without redrawing its letters.

Requires Pillow, NumPy and system libpotrace (a conversion tool, not an app dependency).
Input is the original CourtQuest_Signature_Brand_Pack_v1.zip from DESIGN.md.
No generated transparent drafts or bitmap retouching is used in the app assets.
"""
import argparse
import ctypes as C
import hashlib
import io
import json
from pathlib import Path
from zipfile import ZipFile
import numpy as np
from PIL import Image

class Point(C.Structure):
    _fields_ = [("x", C.c_double), ("y", C.c_double)]

class Curve(C.Structure):
    _fields_ = [("n", C.c_int), ("tag", C.POINTER(C.c_int)), ("c", C.POINTER(Point * 3))]

class PathNode(C.Structure):
    pass

PathNode._fields_ = [("area", C.c_int), ("sign", C.c_int), ("curve", Curve), ("next", C.POINTER(PathNode))]

class State(C.Structure):
    _fields_ = [("status", C.c_int), ("plist", C.POINTER(PathNode))]

class Bitmap(C.Structure):
    _fields_ = [("w", C.c_int), ("h", C.c_int), ("dy", C.c_int), ("map", C.POINTER(C.c_ulong))]

def trace(mask):
    lib = C.CDLL("libpotrace.so.0")
    lib.potrace_param_default.restype = C.c_void_p
    lib.potrace_trace.argtypes = [C.c_void_p, C.POINTER(Bitmap)]
    lib.potrace_trace.restype = C.POINTER(State)
    lib.potrace_state_free.argtypes = [C.POINTER(State)]
    lib.potrace_param_free.argtypes = [C.c_void_p]
    h, w = mask.shape
    padded = np.pad(mask, ((0, 0), (0, (-w) % 64)))
    words = np.packbits(padded, axis=1).view(">u8").astype(np.uint64)
    bitmap = Bitmap(w, h, words.shape[1], words.ctypes.data_as(C.POINTER(C.c_ulong)))
    params = lib.potrace_param_default()
    state = lib.potrace_trace(params, C.byref(bitmap))
    if not state or state.contents.status != 0:
        raise RuntimeError("Signature contour trace failed")
    def xy(p): return f"{p.x:.2f},{p.y:.2f}"
    paths = []
    node = state.contents.plist
    while node:
        curve = node.contents.curve
        points = ["M" + xy(curve.c[curve.n - 1][2])]
        for i in range(curve.n):
            c = curve.c[i]
            if curve.tag[i] == 2:  # POTRACE_CORNER; POTRACE_CURVETO is 1
                points.extend(("L" + xy(c[1]), "L" + xy(c[2])))
            else:
                points.append("C" + " ".join(xy(p) for p in c))
        points.append("Z")
        paths.append(" ".join(points))
        node = node.contents.next
    lib.potrace_state_free(state)
    lib.potrace_param_free(params)
    return " ".join(paths)

def vector(width, height, layers, safe_scale=None):
    display_width, display_height = (160, 160 * height / width) if width != height else (108, 108)
    result = [f'<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="{display_width}dp" android:height="{display_height}dp" android:viewportWidth="{width}" android:viewportHeight="{height}">']
    if safe_scale:
        result.append(f'<group android:pivotX="{width/2}" android:pivotY="{height/2}" android:scaleX="{safe_scale}" android:scaleY="{safe_scale}">')
    for color, data in layers:
        result.append(f'<path android:fillColor="{color}" android:pathData="{data}"/>')
    if safe_scale: result.append('</group>')
    return "\n".join(result + ['</vector>', ''])

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('brand_pack', type=Path)
    parser.add_argument('--output', type=Path, default=Path(__file__).resolve().parents[2] / 'app/src/main/res/drawable')
    args = parser.parse_args()
    with ZipFile(args.brand_pack) as pack:
        raw = pack.read('CourtQuest_Signature_Brand_Pack_v1/CourtQuest_Signature_Approved_Reference.png')
    image = np.asarray(Image.open(io.BytesIO(raw)).convert('RGB')).astype(np.int16)
    assert image.shape == (1254, 1254, 3), 'Use the original approved reference'
    # Measured bounds exclude the presentation label and paper. These are vector
    # coordinate windows into the original; no replacement type or emblem is drawn.
    word = image[800:1190, :, :]
    letters = np.max(word, axis=2) < 110
    orange = (word[:, :, 0] > 190) & (word[:, :, 1] < 170) & (word[:, :, 2] < 110)
    icon = image[153:768, 320:935, :]
    yy, xx = np.mgrid[:615, :615]
    # Exclude the presentation board outside the icon's rounded orange field.
    inside = np.maximum(np.abs(xx - 307.5) - 137.5, 0) ** 2 + np.maximum(np.abs(yy - 307.5) - 137.5, 0) ** 2 <= 170 ** 2
    cream = (np.min(icon, axis=2) > 185) & inside
    seams = (np.max(icon, axis=2) < 110) & ((np.max(icon, axis=2) - np.min(icon, axis=2)) < 40)
    word_layers = [('#FFF5EFE6', trace(letters)), ('#FFFF650E', trace(orange))]
    icon_layers = [('#FFF5EFE6', trace(cream)), ('#FF171718', trace(seams))]
    outputs = {
        'courtquest_signature_wordmark.xml': vector(1254, 390, word_layers),
        'courtquest_signature_emblem.xml': vector(615, 615, icon_layers),
        'courtquest_signature_launcher.xml': vector(615, 615, icon_layers, 0.65),
    }
    # Keep exact vector masters for reproduction; render dense transparent assets
    # once at build-authoring time so Android does not repeatedly parse brush paths.
    masters = Path(__file__).parent / 'vectors'
    masters.mkdir(parents=True, exist_ok=True)
    for name, content in outputs.items(): (masters / name).write_text(content)
    ca = C.CDLL('libcairo.so.2'); rs = C.CDLL('librsvg-2.so.2')
    ca.cairo_image_surface_create.argtypes = [C.c_int, C.c_int, C.c_int]
    ca.cairo_image_surface_create.restype = C.c_void_p
    ca.cairo_create.argtypes = [C.c_void_p]; ca.cairo_create.restype = C.c_void_p
    ca.cairo_surface_write_to_png.argtypes = [C.c_void_p, C.c_char_p]
    ca.cairo_destroy.argtypes = [C.c_void_p]; ca.cairo_surface_destroy.argtypes = [C.c_void_p]
    rs.rsvg_handle_new_from_data.argtypes = [C.c_char_p, C.c_size_t, C.c_void_p]
    rs.rsvg_handle_new_from_data.restype = C.c_void_p
    rs.rsvg_handle_render_cairo.argtypes = [C.c_void_p, C.c_void_p]
    rs.rsvg_handle_render_cairo.restype = C.c_int
    go = C.CDLL('libgobject-2.0.so.0'); go.g_object_unref.argtypes = [C.c_void_p]
    density_dir = args.output.parent / 'drawable-xxxhdpi'
    density_dir.mkdir(parents=True, exist_ok=True)
    rendered = {}
    for name, width, height, layers, scale, pixels in [
        ('wordmark', 1254, 390, word_layers, 1, (836,260)),
        ('emblem', 615, 615, icon_layers, 1, (432,432)),
        ('launcher', 615, 615, icon_layers, 0.65, (432,432))]:
        paths = ''.join(f'<path fill="#{color[-6:]}" d="{data}"/>' for color, data in layers)
        transform = f'translate({width/2} {height/2}) scale({scale}) translate({-width/2} {-height/2})'
        svg = f'<svg xmlns="http://www.w3.org/2000/svg" width="{pixels[0]}" height="{pixels[1]}" viewBox="0 0 {width} {height}"><g transform="{transform}">{paths}</g></svg>'.encode()
        surface = ca.cairo_image_surface_create(0, *pixels); ctx = ca.cairo_create(surface)
        handle = rs.rsvg_handle_new_from_data(svg, len(svg), None)
        assert handle and rs.rsvg_handle_render_cairo(handle, ctx), 'Brand render failed'
        destination = density_dir / f'courtquest_signature_{name}.png'
        assert ca.cairo_surface_write_to_png(surface, str(destination).encode()) == 0
        rendered[destination.name] = hashlib.sha256(destination.read_bytes()).hexdigest()
        go.g_object_unref(handle); ca.cairo_destroy(ctx); ca.cairo_surface_destroy(surface)
    # Do not package duplicate vector definitions of these same resource names.
    for name in outputs:
        existing = args.output / name
        if existing.exists(): existing.unlink()
    manifest = dict(source='CourtQuest_Signature_Approved_Reference.png',
                    source_sha256=hashlib.sha256(raw).hexdigest(),
                    method='Original-reference contour tracing to native VectorDrawable; no generative letterforms',
                    rejected='Transparent image-generation attempts with visible residue',
                    wordmark_viewport=[1254,390], emblem_viewport=[615,615], adaptive_foreground_scale=0.65,
                    outputs={name:hashlib.sha256(content.encode()).hexdigest() for name,content in outputs.items()},
                    android_xxxhdpi_renders=rendered)
    (Path(__file__).parent / 'signature-assets.json').write_text(json.dumps(manifest, indent=2)+'\n')
    print(json.dumps(manifest, indent=2))

if __name__ == '__main__': main()
