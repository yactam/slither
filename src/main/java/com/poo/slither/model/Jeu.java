package com.poo.slither.model;

import com.poo.slither.view.GameView;
import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.poo.slither.model.CollisionUtils.collisionSerpents;
import static com.poo.slither.view.SegmentView.SEGMENT_SIZE;

public final class Jeu implements Serializable {
    public static final int MAP_WIDTH = 5000;
    public static final int MAP_HEIGHT = 5000;
    private final List<Serpent> serpents;
    private final List<Nourriture> nourritures;
    private final int nb_food;
    private final int nb_ia;
    public Jeu(int nb_ai, int nb_food) {
        this.serpents = new ArrayList<>();
        this.nourritures = new ArrayList<>();
        this.nb_food = nb_food;
        this.nb_ia = nb_ai;
        genereNourritures();
        genereIAs();
    }

    public void addSerpent(Serpent serpent) {
        serpents.add(0, serpent);
    }
    private void removeSerpent(Serpent serpent) {
        serpents.remove(serpent);
    }

    public void addNourriture(Nourriture nourriture) {
        nourritures.add(nourriture);
    }

    public List<Serpent> getSerpents() {
        return new ArrayList<>(serpents);
    }

    public List<Nourriture> getFood() {
        return nourritures;
    }

    private void genereNourritures() {
        for (int i = 0; i < nb_food; i++) {
            addNourriture();
        }
    }

    private void genereIAs() {
        for(int i = 0; i < nb_ia; i++) {
            Random random = new Random();
            double x = random.nextDouble() * (MAP_WIDTH - 100);
            double y = random.nextDouble() * (MAP_HEIGHT - 100);
            SerpentIA serpentIA = new SerpentIA(x, y);
            addSerpent(serpentIA);
        }
    }

    private void addNourriture() {
        Random random = new Random();
        double x = random.nextDouble() * (MAP_WIDTH - 50);
        double y = random.nextDouble() * (MAP_HEIGHT - 50);
        Nourriture nourriture;

        int randomType;
        if (Math.random() < 0.05) {
            // 5% de chance pour générer Bouclier
            randomType = 3;
        } else if (Math.random() < 0.1) {
            randomType = 4;
        } else {
            randomType = new Random().nextInt(3);
        }

        nourriture = switch (randomType) {
            case 0 -> new NourritureSimple(x, y);
            case 1 -> new NourritureVitesse(x, y);
            case 2 -> new NourriturePoison(x, y);
            case 3 -> new NourritureBouclier(x, y);
            case 4 -> new NourriturePont(x, y);
            default -> throw new IllegalStateException("Unexpected value: " + randomType);
        };

        addNourriture(nourriture);
    }

    public List<Serpent> updateGame() {
        moveSerpents();
        checkCollisions();
        return updateSerpents();
    }

    private List<Serpent> updateSerpents() {
        List<Serpent> dead = new ArrayList<>();
        for(Serpent serpent : getSerpents()) {
            if(!serpent.isAlive()) {
                removeSerpent(serpent);
                dead.add(serpent);
            }
        }
        return dead;
    }

    private void moveSerpents() {
        for(Serpent serpent : getSerpents()) {
            if(serpent.isAlive())
                serpent.bouge();
        }
    }

    private void checkCollisions() {
        collisionsSerpents();
        collisionsNourritures();
    }

    private void collisionsNourritures() {
        for (Serpent serpent : getSerpents()) {
            List<Nourriture> eatenFood = new ArrayList<>();
            boolean isDead = false;
            for (Nourriture food : getFood()) {
                if (CollisionUtils.collisionNourriture(serpent.getTete(), food)) {
                    isDead = food.applyEffect(serpent);
                    eatenFood.add(food);
                }
            }
            if(isDead) {
                addNourriture();
            }
            nourritures.removeAll(eatenFood);
        }
    }

    private void collisionsSerpents() {
        for (Serpent serpent : serpents) {
            double serpentX = serpent.getTete().getX();
            double serpentY = serpent.getTete().getY();
            if (serpentX < 0 || serpentX >= MAP_WIDTH - SEGMENT_SIZE / 2 || serpentY < 0 || serpentY >= MAP_HEIGHT - SEGMENT_SIZE / 2 ) {
                for(Segment segment : serpent.getSegments()) {
                    addNourriture(segment.toFood());
                }
                serpent.meurt();
            }
        }

        for (Serpent snakeA : serpents) {
            for (Serpent snakeB : serpents) {
                if(snakeA != snakeB) {
                    Segment segmentVictime = collisionSerpents(snakeA, snakeB);
                    if (segmentVictime != null) {
                        boolean isDead = segmentVictime.handelCollision(snakeA, snakeB);
                        if(isDead) {
                            for(Segment segment : snakeA.getSegments()) {
                                addNourriture(segment.toFood());
                            }
                            snakeA.meurt();
                        }
                    }
                }
            }
        }
    }

    public void clearFood() {
    }

    public void clearSerpents() {
    }
}
