package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class MainClass {
    public static final int CARS_COUNT = 4;
    public static void main(String[] args) {
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");



        CyclicBarrier cyclicBarrier = new CyclicBarrier(CARS_COUNT);
        CountDownLatch countDownLatchStart = new CountDownLatch(CARS_COUNT);
        CountDownLatch countDownLatchFinish = new CountDownLatch(CARS_COUNT);
        Semaphore semaphore = new Semaphore(CARS_COUNT/2, true);

        Race race = new Race(new Road(60), new Tunnel(semaphore), new Road(40));

        Car[] cars = new Car[CARS_COUNT];

        for (int i = 0; i < cars.length; i++) {

            cars[i] = new Car(race, 20 + (int) (Math.random() * 10), cyclicBarrier, countDownLatchStart, countDownLatchFinish);


        }



        for (int i = 0; i < cars.length; i++) {
            new Thread(cars[i]).start();
        }


        try {
            countDownLatchStart.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");

        try {
            countDownLatchFinish.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");
    }
}




class Car implements Runnable {
    private static int CARS_COUNT;



    static {
        CARS_COUNT = 0;
    }
    private Race race;
    private int speed;
    private String name;

    CyclicBarrier cyclicBarrier;
    CountDownLatch countDownLatchFinish;
    CountDownLatch countDownLatchStart;

    private static Boolean win = false;


    public String getName() {
        return name;
    }
    public int getSpeed() {
        return speed;
    }
    public Car(Race race, int speed, CyclicBarrier cyclicBarrier, CountDownLatch countDownLatchStart, CountDownLatch countDownLatchFinish) {
        this.countDownLatchFinish = countDownLatchFinish;
        this.countDownLatchStart = countDownLatchStart;
        this.cyclicBarrier = cyclicBarrier;
        this.race = race;
        this.speed = speed;
        CARS_COUNT++;
        this.name = "Участник #" + CARS_COUNT;
    }



    @Override
    public void run() {
        try {
            System.out.println(this.name + " готовится");
            Thread.sleep(500 + (int)(Math.random() * 800));
            System.out.println(this.name + " готов");
            countDownLatchStart.countDown();
            cyclicBarrier.await();


        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < race.getStages().size(); i++) {
            race.getStages().get(i).go(this);
        }
        countDownLatchFinish.countDown();


        synchronized (win){
            if (!win) {
                System.out.println(this.name + " WIN");
                win = true;
            }
        }
    }
}


abstract class Stage {
    protected int length;
    protected String description;
    public String getDescription() {
        return description;
    }
    public abstract void go(Car c);
}



class Road extends Stage {
    public Road(int length) {
        this.length = length;
        this.description = "Дорога " + length + " метров";
    }



    @Override
    public void go(Car c) {
        try {
            System.out.println(c.getName() + " начал этап: " + description);
            Thread.sleep(length / c.getSpeed() * 1000);
            System.out.println(c.getName() + " закончил этап: " + description);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



class Tunnel extends Stage {

    Semaphore semaphore;

    public Tunnel(Semaphore semaphore) {
        this.semaphore = semaphore;
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
    }
    @Override
    public void go(Car c) {
        try {
            try {
                System.out.println(c.getName() + " готовится к этапу(ждет): " + description);

                semaphore.acquire();

                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                semaphore.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class Race {
    private ArrayList<Stage> stages;
    public ArrayList<Stage> getStages() {
        return stages; }
    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
    }
}