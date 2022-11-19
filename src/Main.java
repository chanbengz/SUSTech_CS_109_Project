package src;
import ChessBoard.*;
import GUI.MainFrame;

import javax.swing.*;
import java.io.IOException;
import java.util.Scanner;

public class Main
{
    static void test0()
    {
        Player Alice=new Player("Alice",0);
        String dir;
        try {
            dir=FileOperation.SaveUser(Alice);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Player Bob=new Player();
        String user;
        try {
            user = FileOperation.Load(dir);
        } catch (ChessException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String name=dir.substring(dir.lastIndexOf("/")+1);
        Bob.Load(user,name.substring(0,name.length()-4));
        Bob.Show();
    }
    static void test1()
    {
        Network net=new Network();
        Scanner input=new Scanner(System.in);
        System.out.print("Port: ");
        int port=input.nextInt();
        try {
            net.ServerSetup(port);
            net.Send("Server");
            String data=net.Receive();
            System.out.println(data);
            net.ServerShutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static void test2()
    {
        Network net=new Network();
        Scanner input=new Scanner(System.in);
        System.out.println("IP Port");
        String ip=input.next();
        int port=input.nextInt();
        try {
            net.ClientSetup(ip,port);
            net.Send("Client");
            String data=net.Receive();
            System.out.println(data);
            net.ClientShutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static void test3()
    {
        Player Tim=new Player("Tim",0);
        Player AI=new Player("AI",1);
        ChessBoard Game=new ChessBoard();
        Game.Init(Tim,AI);
        String dir=Game.Play();
        ChessBoard Replay=new ChessBoard();
        String data;
        try {
            data=FileOperation.Load(dir);
        } catch (ChessException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String name=dir.substring(dir.lastIndexOf("/")+1);
        Replay.LoadReplay(data,name.substring(0,name.length()-6));
        Replay.Replay();
    }
    static void test4()
    {
        ChessBoard Game=new ChessBoard();
        String dir,data,name;
        Scanner input=new Scanner(System.in);
        dir=input.next();
        try {
            data=FileOperation.Load(dir);
        } catch (ChessException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        name=dir.substring(dir.lastIndexOf("/")+1);
        try {
            Game.GameContinue(data,name.substring(0,name.length()-5));
        } catch (ChessException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("Save at"+Game.Play());
    }
    static void test5()
    {
        Player AI1=new Player("Easy",1);
        Player AI2=new Player("Normal",2);
        ChessBoard Game=new ChessBoard();
        Game.Init(AI1,AI2);
        String dir=Game.Play();
        System.out.println(dir);
    }
    static void test6(){
        /* Player Tim=new Player("Tim",0);
        Player AI=new Player("AI",1);
        ChessBoard Game=new ChessBoard();
        Game.Init(Tim,AI);
        String dir=Game.Play(); */
        JFrame start = new MainFrame("DarkChess");
    }
    public static void main(String[] args)
    {
        Scanner input=new Scanner(System.in);
        int ack=0;
        while(ack==0)
        {
            System.out.println("input flag:");
            int flag=input.nextInt();
            switch (flag)
            {
                case 0 -> test0();
                case 1 -> test1();
                case 2 -> test2();
                case 3 -> test3();
                case 4 -> test4();
                case 5 -> test5();
                case 6 -> test6();
                default -> ack=1;
            }
        }
    }
}