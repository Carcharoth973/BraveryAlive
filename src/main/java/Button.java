import processing.core.*;

class Button extends PApplet{

    private final float posX;
    private final float posY;
    private final float breite;
    private final float hoehe;
    private int[] currentColor;
    private boolean pressed;
    private int[] strokeColor;
    private final int mode;
    private String name;

    Button(float posX, float posY, float breite, float hoehe, int mode, String name) {
        this.breite = breite;
        this.hoehe = hoehe;
        this.posX = posX;
        this.posY = posY;
        this.mode = mode;
        this.name = name;
        currentColor = new int[] {255,255,255};
        strokeColor = new int[] {180,180,180};
    }


    public boolean drawMe(Button b, PShape s, PApplet p) {
        setColor(p);
        p.strokeWeight(1);
        p.stroke(strokeColor[0],strokeColor[1],strokeColor[2]);
        p.fill(currentColor[0],currentColor[1],currentColor[2]);
        switch (name) {
            case "copy":
                p.translate(0, 0, -50);
                p.shapeMode(CENTER);
                p.ellipseMode(CORNER);
                p.ellipse(posX - 13, posY, breite, hoehe);
                p.translate(0, 0, 50);
                p.shape(s, posX - 1, posY + 2);
                break;
            case "reroll":
                p.translate(0, 0, -50);
                p.shapeMode(CORNER);
                p.ellipseMode(CORNER);
                p.ellipse(posX - 36, posY, breite, hoehe);
                p.translate(0, 0, 50);
                p.shape(s, posX, posY);
                if (inMargin(p)) {
                    return true;
                }
                break;
            case "export":
                p.translate(0, 0, -50);
                p.shapeMode(CENTER);
                p.ellipseMode(CORNER);
                p.ellipse(posX - 26, posY, breite, hoehe);
                p.translate(0, 0, 50);
                p.shape(s, posX, posY);
                break;
        }
        return false;
    }

    public boolean inMargin(PApplet p) {
        if( mode == ELLIPSE) {
            return p.mouseX > posX - breite / 2 && p.mouseX < posX + breite / 2 && p.mouseY > posY - hoehe / 2 && p.mouseY < posY + hoehe / 2;
        } else if (mode == RECT) {
            return p.mouseX > posX && p.mouseX < posX + breite && p.mouseY > posY && p.mouseY < posY + hoehe;
        }
        return false;
    }


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


    public void setPressed(boolean b) {
        pressed = b;
    }

    public boolean getPressed() {
        return pressed;
    }

}
