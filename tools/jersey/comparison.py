"""Build a four-column garment sheet exclusively from instrumented Android window captures."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import argparse


def sheet(folder, target):
    root = Path(__file__).resolve().parents[2]
    fonts = root / 'app/src/main/res/font'
    title = ImageFont.truetype(str(fonts/'barlow_condensed_extra_bold.ttf'), 48)
    heading = ImageFont.truetype(str(fonts/'barlow_condensed_extra_bold.ttf'), 34)
    small = ImageFont.truetype(str(fonts/'barlow_regular.ttf'), 22)
    styles = ['arena','classic','fastbreak','retro']
    rows = [('default','front','Home · front'),('default','back','Home · back'),('custom','front','Custom · front'),('custom','back','Custom · back'),('plain','front','No chest text')]
    w, gap, row = 395, 10, 470
    out = Image.new('RGB',(40 + (w+gap)*4, 152 + row*len(rows)), '#0B0C0D'); d = ImageDraw.Draw(out)
    d.text((20,12),'CourtQuest Pro  /  Jersey Studio',font=title,fill='#F7F4EF');d.text((21,71),'v57  ·  Android emulator  ·  Same colors across all four cuts',font=small,fill='#B8BBC3')
    for i,style in enumerate(styles): d.text((22+i*(w+gap),111),style.capitalize(),font=heading,fill='#FF914A')
    for j,(group,side,label) in enumerate(rows):
        top=154+j*row
        for i,style in enumerate(styles):
            im=Image.open(folder/f'{group}-{style}-{side}-garment.png').convert('RGB')
            # Preserve the garment's aspect and the complete displayed print area. Trim stage wings only.
            center=im.width//2; half=min(center,round(im.height*.45)); im=im.crop((center-half,0,center+half,im.height))
            im.thumbnail((w,row-36),Image.Resampling.LANCZOS); x=20+i*(w+gap)
            out.paste(im,(x+(w-im.width)//2,top+26)); d.text((x,top),label,font=small,fill='#B8BBC3')
    out.save(target,optimize=True)


if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('folder',type=Path);parser.add_argument('target',type=Path)
    args=parser.parse_args();sheet(args.folder,args.target)
