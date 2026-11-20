"""Anatomical skin registration in the athlete master's 1024 px coordinate space."""
import cv2
import numpy as np
from PIL import Image,ImageDraw,ImageFilter

# Ball silhouettes have hard exclusions. The fingers gripping each ball are
# traced separately, retaining player skin even when it passes over orange leather.
BALLS={
 "standing":((299,440,419,560),[
 [(291,444),(333,435),(346,434),(337,448),(326,460),(326,473),(340,491),(349,507),(355,522),(351,526),(345,525),(330,506),(320,493),(321,512),(329,527),(341,540),(356,549),(357,554),(350,556),(333,547),(329,546),(354,557),(354,561),(342,559),(317,546),(302,529),(295,508),(290,479)]]),
 "dribbling":((723,670,872,819),[
 [(754,608),(789,607),(800,624),(812,633),(825,646),(840,656),(853,674),(866,692),(866,698),(859,692),(849,680),(837,672),(843,680),(850,686),(849,689),(832,677),(822,672),(812,660),(795,651),(784,649),(783,674),(778,682),(772,681),(768,676),(769,651),(762,633)]]),
 "action":((580,85,708,208),[
 [(541,205),(555,184),(566,155),(571,139),(580,127),(584,128),(582,139),(577,153),(581,155),(591,137),(606,119),(611,117),(613,120),(609,127),(597,140),(590,151),(607,144),(623,135),(635,130),(640,131),(639,135),(622,145),(608,154),(596,164),(618,157),(637,149),(641,150),(641,154),(636,157),(614,166),(598,174),(616,173),(635,172),(641,173),(640,177),(634,180),(606,182),(594,185),(581,194),(570,204),(553,218)],
 [(650,213),(660,202),(674,193),(684,181),(682,170),(684,167),(689,170),(692,182),(698,168),(700,148),(703,146),(706,149),(705,171),(701,189),(692,203),(677,217),(668,227)]])}

# Below each sock seam the athlete has no exposed skin. The raised action leg
# needs its own boundary, well above that of the grounded foot.
SOCKS={"standing":(501,831,833),"dribbling":(480,813,808),"action":(501,837,565),
       "ready":(504,800,800),"followthrough":(508,827,826)}

def limb_skin(body,old,cloth,head,pose):
    rgb=np.asarray(body.convert("RGB")).astype(np.int16)
    r,g,b=np.moveaxis(rgb,-1,0)
    alpha=np.asarray(body.getchannel("A"))
    yy,xx=np.indices(alpha.shape); split,y1,y2=SOCKS[pose]
    in_play=(yy<np.where(xx<split,y1,y2)) & (alpha>0)
    head_px=np.asarray(head)>0
    warm=(r-g>=3)&(r>g*1.10)&(r>b*1.18)
    # White/grey cloth is rejected as a material even when the coarse garment
    # control polygons do not reach the cut seam. Shadows and highlights are
    # recovered by GrabCut from their adjacent, labeled pieces of anatomy.
    old_px=np.asarray(old)>80
    cloth_px=np.asarray(cloth)>140
    initial=np.where(warm | old_px | head_px,cv2.GC_PR_FGD,cv2.GC_PR_BGD).astype('uint8')
    initial[warm & in_play & ~cloth_px & (np.asarray(old.filter(ImageFilter.MaxFilter(21)))>100)]=cv2.GC_FGD
    initial[~in_play | (cloth_px & ~warm) | ((abs(r-g)<3)&(abs(g-b)<4)&(r>88)&~head_px)]=cv2.GC_BGD
    ball=np.zeros_like(alpha);hand=np.zeros_like(alpha)
    if pose in BALLS:
        bounds,fingers=BALLS[pose];im=Image.new("L",body.size);ImageDraw.Draw(im).ellipse(bounds,fill=255);ball=np.asarray(im)
        im=Image.new("L",body.size)
        for finger in fingers:ImageDraw.Draw(im).polygon(finger,fill=255)
        hand=np.asarray(im)
        initial[(ball>0)&(hand==0)]=cv2.GC_BGD
        initial[(hand>0)&in_play]=cv2.GC_PR_FGD
    initial[head_px & in_play]=cv2.GC_FGD
    cv2.setRNGSeed(0)
    cv2.grabCut(np.ascontiguousarray(np.asarray(body.convert("RGB"))),initial,None,
                np.zeros((1,65),np.float64),np.zeros((1,65),np.float64),3,cv2.GC_INIT_WITH_MASK)
    mask=np.where(((initial==cv2.GC_FGD)|(initial==cv2.GC_PR_FGD)) & in_play,255,0).astype('uint8')
    # Preserve isolated warm fingertip highlights and hard ball boundaries.
    mask[(hand>0)&in_play]=255
    mask[(ball>0)&(hand==0)]=0
    return Image.fromarray(mask).filter(ImageFilter.GaussianBlur(.5))
