package GUI;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class PieceComponent extends JButton {
    public boolean isRevealed;
    public boolean selected;
    public int rank;
    public int player;
    public int x, y;
    public boolean [][]validCord = new boolean[6][10];

    public PieceComponent(int player, int rank) {
        this.rank = rank;
        this.player = player;
        selected = false;
        isRevealed = false;
        this.setIcon(new ImageIcon("resources/hide.png"));
        this.setBorder(null);
        this.setOpaque(false);
        this.setContentAreaFilled(false);
        this.setSize(75,75);
    }

    public boolean canMoveTo(PieceComponent target) {
        if(target.isRevealed||target.player == this.player||target.rank > this.rank) {
            return false;
        } else if(!validCord[target.x][target.y]) {
            return false;
        } else {
            return true;
        }
    }

    public void Move2(PieceComponent target) {
        this.transfer2(target);
        this.empty();
        target.update();
    }

    public void Reveal() {
        this.isRevealed = true;
        update();
    }

    public void transfer2(PieceComponent target) {
        target.player = this.player;
        target.rank = this.rank;
        target.isRevealed = this.isRevealed;
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 9; j++) {
                validCord[i][j] = false;
            }
        }
    }

    public void empty() {
        isRevealed = true;
        selected = false;
        rank = 0;
        player = -1;
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 9; j++) {
                validCord[i][j] = false;
            }
        }
        this.setVisible(false);
    }

    private void update() {
        this.setIcon(new ImageIcon(this.getPath()));
        this.setBorder(null);
        this.setOpaque(false);
        this.setContentAreaFilled(false);
        this.repaint();
    }

    public String getPath() {
        String color = this.player == 0 ? "B" : "R";
        return "resources/" + color + Integer.toString(rank) + ".png";
    }
}
