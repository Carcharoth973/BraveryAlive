import processing.core.*;

class Button extends PApplet{

    private final float posX, posY, width, height;
    private int[] currentColor, strokeColor;
    private boolean pressed;
    private final PShape pShape;
    private final int ellipseMode, shapeMode, form;

    Button(float posX, float posY, float width, float height, int form, int ellipseMode, int shapeMode, PShape pShape) {
        this.width = width;
        this.height = height;
        this.posX = posX;
        this.posY = posY;
        this.form = form;
        this.shapeMode = shapeMode;
        this.ellipseMode = ellipseMode;
        this.pShape = pShape;
        currentColor = new int[] {255,255,255};
        strokeColor = new int[] {180,180,180};
    }

    //draw the Button in the given PApplet
    public void drawMe(PApplet p) {
        setColor(p);
        p.strokeWeight(1);
        p.stroke(strokeColor[0],strokeColor[1],strokeColor[2]);
        p.fill(currentColor[0],currentColor[1],currentColor[2]);
        p.ellipseMode(ellipseMode);
        p.ellipse(posX, posY, this.width, this.height);
        p.shapeMode(shapeMode);
        p.pushMatrix();
         if(posX < p.width/2f)
         p.translate(0,-36,50);
         else if (posX == p.width/2f)
         p.translate(0,-25,50);
         else if(posX > p.width/2f)
         p.translate(-12,-25,50);
        p.shape(pShape,posX,posY);
        p.popMatrix();
    }

    //determine weather the mouse is hovering or holding the button
    public boolean inMargin(PApplet p) {
        if( form == ELLIPSE && this.width == this.height) {
            return dist(p.mouseX, p.mouseY, posX, posY) < this.width/2;
        } else if (form == RECT) {
            return p.mouseX > posX && p.mouseX < posX + this.width && p.mouseY > posY && p.mouseY < posY + this.height;
        }
        return false;
    }

//set the current color depending on mouse position
    private void setColor(PApplet p) {
        if (inMargin(p)) {
            if (pressed) {
                currentColor = new int[] {163, 219, 245};
                strokeColor = new int[] {0, 88, 180};
            } else if (!mousePressed)
                currentColor = new int[] {222, 241, 250};
            strokeColor = new int[] {0, 125, 255};
        } else {
            currentColor =  new int[] {230, 230, 230};
            strokeColor =  new int[] {180, 180, 180};
        }
    }

    public PShape getShape () {
        return pShape;
    }
    public void setPressed(boolean b) {
        pressed = b;
    }
    public boolean getPressed() {
        return pressed;
    }

}
