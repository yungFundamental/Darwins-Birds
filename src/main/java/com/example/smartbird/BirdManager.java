package com.example.smartbird;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BirdManager implements Runnable
{
    private final CommandHandler handler;       // handles addition and removal of birds to the pane.
    private ArrayList<Bird> aliveGeneration;    //the currently alive birds of the current generation
    private ArrayList<Bird> deadGeneration;    //the dead birds of the current generation
    private PipeManager obstacles;
    private final double x;
    private final int generationSize;       //the amount of bird in one generation
    private final double radius;
    private final double floorY;
    private boolean running;
    private final double maxPipeX;      //the maximum x coordinate of a pipe.
    private static final Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.DARKGREY, Color.INDIANRED,
                                            Color.AQUA, Color.DARKTURQUOISE, Color.MISTYROSE, Color.LIGHTGOLDENRODYELLOW,
                                            Color.FIREBRICK};
    private static final int PARAMETERS_COUNT = 5;  // number of parameters for each neural network

    private final double min_weight;
    private final double max_weight;
    private final double min_bias;
    private final double max_bias;





    public BirdManager(CommandHandler commandHandler, PipeManager pipeManager, double x, double floorY, double radius, int generationSize,
                        double maxPipeX, double min_weight, double max_weight, double min_bias, double max_bias) {
        this.handler = commandHandler;
        obstacles = pipeManager;
        this.x = x;
        this.radius = radius;
        this.floorY = floorY;
        this.generationSize = generationSize;
        this.maxPipeX = maxPipeX;

        this.min_weight = min_weight;
        this.max_weight = max_weight;
        this.min_bias = min_bias;
        this.max_bias = max_bias;

        running = false;
        aliveGeneration = new ArrayList<>();
        deadGeneration = new ArrayList<>();
        for (int i = 0; i< generationSize; i++) {
            // input layer - 4 neurons (the parameters will be explained later)
            NeuralNetwork neuralNetwork = new NeuralNetwork(PARAMETERS_COUNT);
            // first hidden layer - 16 neurons, activation function is RelU
            neuralNetwork.addLayer(16, new ReLU());
            // second hidden layer - 16 neurons, activation function is RelU
            neuralNetwork.addLayer(16, new ReLU());
            // output layer - 1 neuron (jump or not)
            // I used the sigmoid function because the answer should be between 0 and 1.
            neuralNetwork.addLayer(1, new Sigmoid());
            //randomize neural network:
            neuralNetwork.randomize(min_weight,max_weight,min_bias,max_bias);

            Bird b = new Bird(x, floorY / 2, radius, colors[i],neuralNetwork);
            aliveGeneration.add(b);
            handler.demand(b, true);
        }
    }

    private Bird select() {
        // the bird with the maximum score will have 100% chance to be selected.
        Bird maxBird = deadGeneration.get(0);
        for (Bird bird: deadGeneration){
            if (maxBird.getScore() < bird.getScore())
                maxBird = bird;
        }
        return maxBird;
    }
    public void stop(){
        running = false;
    }

    /** execute scheduled tasks on every alive bird (move in accordance to speed, accelerate in accordance to gravity)
     *
     * @param gravity Acceleration value (larger the value, the more the speed increases downwards).
     */
    public void step(double gravity){
        List<Bird> birdList = new ArrayList<>(this.aliveGeneration);
        for(Bird bird: birdList){
            bird.accelerate(gravity);
            bird.step();
        }
    }

    @Override
    public void run() {
        running = true;
        double[] input = new double[PARAMETERS_COUNT];
        while (running){

            while (!aliveGeneration.isEmpty()) {    // while the current generation is still alive
                // for each alive bird
                for (int i = 0; i<this.aliveGeneration.size(); i++) {
                    Bird mrBird = aliveGeneration.get(i);
                    PipePair p;
                    // check for death
                    // p = the closest pipe (the only one possible for collision)
                    p = obstacles.getClosestRight(this.x - this.radius);
                    // if he collides with the pipe or hits the floor
                    if (p != null && (mrBird.checkCollision(p) || mrBird.getCenterY() + mrBird.getRadius() >= floorY)) {
                        aliveGeneration.remove(mrBird);
                        deadGeneration.add(mrBird);
                        handler.demand(mrBird, false);          // remove bird from pane.
                    }
                    else {


                        //check neural network:
                        // get parameters
                        //param1: The birds Y coordinate
                        input[0] = mrBird.getCenterY();
                        //param2: The closest pipes x coordinate
                        input[1] = (p != null) ? p.getX() : maxPipeX;
                        //param3: The Y of the closest gap.
                        input[2] = (p != null) ? p.getGapY() : 0;
                        //param4: The y velocity of the bird
                        input[3] = mrBird.getVelocity();
                        //param5: the x velocity of the pipes
                        input[4] = obstacles.getSpeed();

                        // check neuralNetwork
                        if (mrBird.shouldJump(input)) {
                            mrBird.jump();
                        }

                        mrBird.incScore();
                    }
                }
            }   //generation has perished

            Bird fittest = select();
            for (int i = 0; i<generationSize; i++) {
                Bird child = new Bird(x, floorY / 2, radius, colors[i], fittest.getBrain());
                child.mutate(0.1, min_weight,max_weight,min_bias,max_bias);
                aliveGeneration.add(child);
                handler.demand(child, true);
            }
            deadGeneration.clear();
            obstacles.clearList();
        }
    }
}
