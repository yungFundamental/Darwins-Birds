package com.example.smartbird;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PipePair {
    Pipe upper;
    Pipe lower;

    public PipePair(double x, double gapY, double width, double gapHeight, Color color)
    {
        upper = new Pipe(x, true, width, gapY, color);
        lower = new Pipe(x, false, width, HelloApplication.S_HEIGHT - (gapY+gapHeight), color);
    }


    public void moveLeft(double amount)
    {
        upper.moveLeft(amount);
        lower.moveLeft(amount);
    }

    public double getWidth()
    {
        return this.upper.getWidth();
    }

    public void addToPane(Pane p)
    {
        p.getChildren().add(this.upper);
        p.getChildren().add(this.lower);
    }

    public void removeFromPane(Pane p)
    {
        p.getChildren().remove(this.upper);
        p.getChildren().remove(this.lower);
    }

    public Pipe getUpperPipe(){
        return new Pipe(this.upper);
    }

    public Pipe getLowerPipe(){
        return new Pipe(this.lower);
    }



    public double getX()
    {
        return upper.getTranslateX();
    }

    public static class Pipe extends Rectangle {

        /** Copy Constructor.
         *
         * @param o other pipe
         */
        public Pipe(Pipe o)
        {
            super(o.getWidth(), o.getHeight(), o.getFill());
            this.setTranslateX(o.getTranslateX());
            this.setTranslateY(o.getTranslateY());
        }

        public Pipe(double x, boolean isTop, double width, double height, Color color){
            super(width, height, color);
            this.setTranslateX(x);
            this.setTranslateY((isTop)?0:HelloApplication.S_HEIGHT-height);
        }


        public void moveLeft(double amount)
        {
            setTranslateX(getTranslateX()-amount);
        }


    }
}