"""Build registered, independently dyeable head, skin and shoe layers from the preserved athlete masters."""
import argparse
import io
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter, ImageOps
from build_assets import OUT, SOURCES, POSES as ARTWORK_POSES
from build_footwear_styles import POSES, read_cqp
from build_head_styles import REGIONS, STYLES
from build_premium_parts import FEET
from build_face_blank_assets import create as add_anonymous_face, check as check_anonymous_face

# Hairlines are traced on the transparent head crop (percent of crop width/height).
# The outer contour always comes from the authored cutout, including locs and flyaways.
LINES = {
    "standing": [(0,68),(20,58),(26,50),(30,35),(40,30),(58,29),(69,31),(75,38),(77,54),(84,59),(100,68)],
    "dribbling": [(0,35),(19,32),(24,29),(32,30),(47,33),(57,38),(63,49),(72,56),(78,49),(86,50),(100,58)],
    "action": [(0,59),(15,57),(20,50),(26,49),(30,51),(32,42),(39,31),(51,25),(63,23),(72,26),(100,34)],
    "ready": [(0,58),(22,49),(26,40),(31,35),(42,32),(56,32),(67,36),(71,45),(75,52),(82,55),(100,62)],
    "followthrough": [(0,57),(12,47),(23,39),(38,33),(58,31),(75,32),(89,34),(100,38)],
}
# Pose-local foot bounds contain the entire toe and sole. The cuff follows the
# sock/tongue seam. Alpha comes from the original subject, never a rectangular shoe.
CUFFS = {
    "standing": [[(320,912),(348,904),(358,881),(369,886),(377,875),(393,875),(401,881),(415,882),(435,896)],
                 [(590,894),(602,885),(614,887),(619,879),(636,876),(648,884),(661,889),(701,918)]],
    "dribbling": [[(165,884),(227,862),(237,847),(246,842),(260,847),(278,855),(292,847),(302,850)],
                  [(623,845),(637,837),(647,842),(658,848),(675,849),(681,836),(697,832),(706,841),(797,900)]],
    "action": [[(307,891),(325,865),(339,859),(347,866),(365,877),(377,878),(389,868),(395,867)],
               [(585,638),(606,625),(611,623),(624,617),(636,610),(649,610),(661,622),(729,656)]],
    "ready": [[(330,890),(353,859),(363,852),(381,851),(391,858),(402,862),(417,853),(434,868)],
              [(581,869),(595,856),(607,861),(618,853),(632,851),(646,858),(660,877),(682,893)]],
    "followthrough": [[(366,905),(379,879),(392,867),(404,866),(414,872),(426,878),(438,871),(448,884)],
                      [(555,888),(568,876),(578,879),(587,869),(603,868),(615,876),(626,891),(647,905)]],
}
REPLIT = {"brush":"straight","boxbun":"boxbraids","twistknot":"twists","sculpted":"waves"}
SOURCE_STYLE = {"brush":"fade","boxbun":"topknot","twistknot":"topknot","sculpted":"waves"}

def mul(a,b):
    return ImageChops.multiply(a,b)

def alpha(im):
    return im.getchannel("A")

def masked(im, mask):
    im=im.copy()
    im.putalpha(mul(alpha(im),mask))
    return im

def stamp(plane, size, xy):
    out=Image.new(plane.mode,size)
    out.paste(plane,xy)
    return out

def cap(pose, style, head):
    points=LINES[pose].copy()
    if style in ("straight","locs"):
        if pose in ("standing","ready"):
            depth=89 if style=="locs" else 82
            points=[(0,92),(19,depth),(27,depth),(31,48),(36,34),(51,29),(65,34),(71,51),(73,depth),(82,depth),(100,88)]
        elif pose=="dribbling":
            points=[(0,36),(25,30),(38,27),(49,25),(61,32),(69,41),(73,52),(82,62),(100,70)]
        elif pose=="followthrough":
            points=[(0,65),(15,63),(29,58),(42,52),(59,45),(76,36),(89,31),(100,35)]
    if style=="afro":
        points=[(x,min(92,y+(8 if 18<x<83 else 10))) for x,y in points]
    w,h=head.size
    contour=Image.new("L",head.size)
    ImageDraw.Draw(contour).polygon([(0,0),(w,0)]+[(round(x*w/100),round(y*h/100)) for x,y in reversed(points)],fill=255)
    # Keep warm forehead or ear pixels out of the dyeable hair fringe.
    boundary=ImageChops.subtract(contour,contour.filter(ImageFilter.MinFilter(17)))
    reject=Image.frombytes("L",head.size,bytes(255 if r>1.22*g and r-b>15 and r>63 else 0 for r,g,b,a in head.getdata()))
    contour=ImageChops.subtract(contour,mul(boundary,reject))
    return masked(head,contour.filter(ImageFilter.GaussianBlur(.6)))

def extract_foot_mask(pose, body):
    mask=Image.new("L",body.size)
    for box,points in zip(FEET[pose],CUFFS[pose]):
        l,t,r,b=box
        region=Image.new("L",body.size)
        ImageDraw.Draw(region).polygon(points+[(r+2,b+1),(l-2,b+1)],fill=255)
        foot=mul(alpha(body),region.filter(ImageFilter.GaussianBlur(.55)))
        # Keep only the connected foot; the dribbling plate also has a ball near the raised foot.
        solid=foot.point(lambda v:255 if v>15 else 0)
        seeds=[(x,y) for y in range(b-1,t,-1) for x in range(l,r) if solid.getpixel((x,y))]
        if not seeds:
            raise ValueError(f"Empty foot: {pose} {box}")
        ImageDraw.floodfill(solid,seeds[0],128)
        component=solid.point(lambda v:255 if v==128 else 0).filter(ImageFilter.MaxFilter(3))
        mask=ImageChops.lighter(mask,mul(foot,component))
    return mask

def filled_foot(edited, original, sole):
    # Extend original edited material into the bright rubber and lace pixels
    # omitted by v58's luminance-keyed cutouts. Keep fine relief from the actual foot.
    box=sole.getbbox()
    source=edited.crop(box)
    original=original.crop(box)
    valid=alpha(source)
    # Generated edits can have neutral checker/white at the outside contours.
    valid=mul(valid,ImageOps.grayscale(source).point(lambda l:255 if l<218 else 0))
    source.putalpha(valid)
    # Normalized convolution at progressively wider radii. Premultiplied color
    # prevents transparent black from bleeding into white leather and laces.
    filled=Image.new("RGB",source.size)
    rgb=source.convert("RGB")
    for radius in (13,5,1):
        support=valid.filter(ImageFilter.GaussianBlur(radius))
        bands=[]
        for channel in rgb.split():
            weighted=mul(channel,valid).filter(ImageFilter.GaussianBlur(radius))
            bands.append(Image.frombytes("L",source.size,bytes(min(255,round(n*255/max(1,d))) for n,d in zip(weighted.tobytes(),support.tobytes()))))
        filled.paste(Image.merge("RGB",bands),mask=support.point(lambda v:255 if v>6 else 0))
    detail=ImageOps.grayscale(original)
    neutral=ImageOps.colorize(detail,"#15181f","#dbe0e2")
    filled=Image.blend(neutral,filled,.64)
    filled.paste(rgb,mask=valid)
    out=filled.convert("RGBA")
    out.putalpha(sole.crop(box))
    return stamp(out,sole.size,box[:2])

def encode(im):
    buffer=io.BytesIO()
    im.save(buffer,"WEBP",lossless=True,method=4,exact=False)
    return buffer.getvalue()

def write_bundle(pose):
    base=read_cqp(OUT/f"{pose}.pcmat",11)
    body=base[0]
    skin_old=alpha(read_cqp(OUT/f"{pose}-avatar.pcmat",2)[0])
    authored=read_cqp(OUT/f"{pose}-footwear.pcmat",4)
    xs,ys=zip(*REGIONS[pose])
    box=(min(xs),min(ys),max(xs)+1,max(ys)+1)
    head=body.crop(box)
    original_hair=cap(pose,"original",head)
    bare=Image.open(OUT/f"heads/{pose}-bald.webp").convert("RGBA")
    # Use one complete head for every style. The transition is inside the
    # neck, where the same original body can carry the face into the uniform.
    w,h=head.size
    head_region=Image.new("L",body.size)
    ImageDraw.Draw(head_region).polygon(REGIONS[pose],fill=255)
    blend=Image.new("L",head.size)
    for y in range(h):
        value=max(0,min(255,round((h*.95-y)*255/(h*.12))))
        ImageDraw.Draw(blend).line((0,y,w,y),fill=value)
    crown_zone=mul(stamp(blend,body.size,box[:2]),head_region)
    scalp=masked(bare,blend)
    if pose=="followthrough":
        # This pose already has a shaved, continuous cranium behind the raised arms.
        crown_zone=Image.new("L",body.size)
        scalp=Image.new("RGBA",head.size)
    feet=extract_foot_mask(pose,body)
    removed=ImageChops.lighter(crown_zone,feet.point(lambda v:255 if v>0 else 0))
    bare_body=body.copy()
    bare_body.putalpha(mul(alpha(body),ImageOps.invert(removed)))
    bare_body.alpha_composite(scalp,box[:2])
    face_area=Image.new("L",body.size)
    # The original upper head is replaced. Only the canonical scalp can enter this matte.
    head_polygon={
        "standing":[(450,0),(560,0),(561,154),(550,173),(504,194),(465,171),(451,138)],
        "dribbling":[(473,0),(608,0),(611,144),(582,175),(578,190),(543,201),(498,182),(476,164)],
        "action":[(390,0),(499,0),(506,135),(489,161),(448,175),(404,155),(393,113)],
        "ready":[(455,29),(567,29),(567,146),(552,169),(514,192),(474,168),(455,143)],
        "followthrough":[(487,139),(542,132),(565,152),(570,178),(568,208),(553,235),(550,260),(534,277),(502,254),(490,218),(484,194)]}[pose]
    ImageDraw.Draw(face_area).polygon(head_polygon,fill=255)
    face_area=mul(face_area,alpha(bare_body))
    cloth=ImageChops.add(alpha(base[2]),alpha(base[3]))
    from anatomy_skin import limb_skin
    anatomy=limb_skin(bare_body,skin_old,cloth,face_area,pose)
    skin=mul(ImageChops.lighter(ImageChops.subtract(anatomy,cloth),face_area),alpha(bare_body))
    face=mul(face_area,alpha(base[1]).filter(ImageFilter.MaxFilter(3)))
    entries={"erase":removed,"scalp":stamp(scalp,body.size,box[:2]),"skin":skin,"face":face}
    original_shoe=body.copy();original_shoe.putalpha(feet)
    entries["shoe-original"]=original_shoe
    for i,model in enumerate(("low","high","strap"),1):
        # For the high top include the pose-matched collar but only where the athlete exists.
        mask=feet
        if model=="high":
            extension=mul(alpha(authored[i]),alpha(body))
            mask=ImageChops.lighter(mask,mul(extension,feet.filter(ImageFilter.MaxFilter(19))))
        entries["shoe-"+model]=filled_foot(authored[i],body,mask)
    hairs={"original":original_hair,"bald":Image.new("RGBA",head.size)}
    for style in STYLES:
        if style!="bald":
            hairs[style]=cap(pose,style,Image.open(OUT/f"heads/{pose}-{style}.webp").convert("RGBA"))
    for name,source in REPLIT.items():
        master=Image.open(SOURCES/f"player_hair_{source}.png").convert("RGBA")
        master=master.crop(alpha(master).getbbox())
        anchor=hairs[SOURCE_STYLE[name]]
        l,t,r,b=alpha(anchor).getbbox()
        if pose=="dribbling": master=ImageOps.mirror(master)
        # Warp the texture into the already registered crown. This preserves
        # the pose's temples, hairline and outer contour on every view.
        color=ImageOps.fit(master,(r-l,b-t),Image.Resampling.LANCZOS)
        textured=anchor.crop((l,t,r,b)).convert("RGB")
        textured.paste(color.convert("RGB"),mask=alpha(color).point(lambda v:round(v*.72)))
        textured=textured.convert("RGBA")
        textured.putalpha(alpha(anchor).crop((l,t,r,b)))
        hairs[name]=stamp(textured,head.size,(l,t))
    for style,im in hairs.items():
        entries["hair-"+style]=stamp(im,body.size,box[:2])
    if pose in ("action","followthrough"):
        # The old full-head export clipped expanded crowns at its left/top edge.
        # Recover their natural outline from a complete approved view.
        if pose=="action":
            master=Image.open(OUT/"heads/dribbling-afro.webp").convert("RGBA")
            crown=ImageOps.mirror(cap("dribbling","afro",master))
            crown=crown.crop(alpha(crown).getbbox()).resize((118,102),Image.Resampling.LANCZOS)
            entries["hair-afro"]=stamp(crown,body.size,(383,3))
        else:
            with zipfile.ZipFile(OUT/"anatomy/ready.zip") as other:
                mapped=json.loads(other.read("bounds.json"))
                for name in hairs:
                    if name in ("original","bald"): continue
                    key="hair-"+name
                    sprite=Image.open(io.BytesIO(other.read(key+".webp"))).convert("RGBA")
                    l,t,r,b=mapped[key]
                    sprite=sprite.resize((max(1,round((r-l)*.83)),max(1,round((b-t)*.83))),Image.Resampling.LANCZOS)
                    entries[key]=stamp(sprite,body.size,(round(529+(l-513)*.83),round(140+(t-39)*.83)))
    target=OUT/"anatomy"/f"{pose}.zip"
    target.parent.mkdir(exist_ok=True)
    manifest={}
    with zipfile.ZipFile(target,"w",compression=zipfile.ZIP_STORED) as archive:
        def put(name,data):
            entry=zipfile.ZipInfo(name,(1980,1,1,0,0,0));archive.writestr(entry,data)
        for name,im in entries.items():
            bounds=im.getbbox() or (0,0,1,1)
            piece=im.crop(bounds)
            if im.mode=="L":
                # Alpha-only masks allow Porter-Duff composition without reading pixels on device.
                img=Image.new("RGBA",piece.size,"white");img.putalpha(piece);piece=img
            put(name+".webp",encode(piece))
            manifest[name]=bounds
        put("bounds.json",json.dumps(manifest,separators=(",",":")).encode())
    return target

if __name__=="__main__":
    for pose in POSES:
        path=write_bundle(pose)
        source=ARTWORK_POSES[pose]["source"]
        add_anonymous_face(pose,source,path)
        check_anonymous_face(path,source)
        print(path.name,path.stat().st_size,flush=True)
