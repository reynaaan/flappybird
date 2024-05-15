/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author Yna
 */
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird class
    int birdX = boardWidth / 3;
    int birdY = boardWidth / 3;
    int birdWidth = 34;
    int birdHeight = 24;


    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -5;
    int velocityY = 0;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    // Sound clips
    Clip dieClip;
    Clip hitClip;
    Clip wingClip;

    Font flappyfont;
    
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this); // for space key

        loadSoundFiles();

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Load font
        try {
            flappyfont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("flappyfont.TTF"));
            flappyfont = flappyfont.deriveFont(Font.PLAIN, 22);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // Place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // Game timer
        gameLoop = new Timer(1000 / 60, this);
        // Don't start the game automatically
        // gameLoop.start();
    }

    // load sound files
    private void loadSoundFiles() {
        try {
            AudioInputStream dieStream = AudioSystem.getAudioInputStream(getClass().getResource("./sfx_die.wav"));
            dieClip = AudioSystem.getClip();
            dieClip.open(dieStream);

            AudioInputStream hitStream = AudioSystem.getAudioInputStream(getClass().getResource("./sfx_hit.wav"));
            hitClip = AudioSystem.getClip();
            hitClip.open(hitStream);

            AudioInputStream wingStream = AudioSystem.getAudioInputStream(getClass().getResource("./sfx_wing.wav"));
            wingClip = AudioSystem.getClip();
            wingClip.open(wingStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // play die sound
    private void playDieSound() {
        if (dieClip != null && !dieClip.isRunning()) {
            dieClip.setFramePosition(0);
            dieClip.start();
        }
    }

    // Play hit sound
    private void playHitSound() {
        if (hitClip != null && !hitClip.isRunning()) {
            hitClip.setFramePosition(0);
            hitClip.start();
        }
    }

    // Play wing sound
    private void playWingSound() {
        if (wingClip != null && !wingClip.isRunning()) {
            wingClip.setFramePosition(0);
            wingClip.start();
        }
    }

    // Place pipes
    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Draw(g);
    }

    public void Draw(Graphics g) {
        // background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // score
        g.setColor(Color.white);
        g.setFont(flappyfont); //custom font
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
        if (!gameLoop.isRunning()) {
            g.drawString("Press SPACE to start", 80, 200);
        }
    }

    public void Move() {
        // bird movement
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // pipes movement
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
                playHitSound();
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
            playDieSound();
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            } else {
                playWingSound();
                if (!gameLoop.isRunning()) {
                    gameLoop.start();
                }
            }
        }
    }
        // Unused methods
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        flappybird = new javax.swing.JLabel();
        toppipe = new javax.swing.JLabel();
        bottompipe = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        Background = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

        setLayout(null);

        flappybird.setIcon(new javax.swing.ImageIcon(getClass().getResource("/flappybird.png"))); // NOI18N
        flappybird.setPreferredSize(new java.awt.Dimension(34, 54));
        add(flappybird);
        flappybird.setBounds(60, 280, 40, 30);

        toppipe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toppipe.png"))); // NOI18N
        add(toppipe);
        toppipe.setBounds(190, -210, 64, 520);

        bottompipe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bottompipe.png"))); // NOI18N
        add(bottompipe);
        bottompipe.setBounds(190, 422, 64, 510);

        jButton1.setText("PLAY");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1);
        jButton1.setBounds(20, 60, 120, 40);

        jPanel1.setLayout(new javax.swing.OverlayLayout(jPanel1));
        add(jPanel1);
        jPanel1.setBounds(0, 320, 0, 0);

        Background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/flappybirdbg.png"))); // NOI18N
        add(Background);
        Background.setBounds(0, 0, 360, 640);

        jButton3.setText("jButton3");
        add(jButton3);
        jButton3.setBounds(25, 140, 120, 40);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Flappy Bird");
                FlappyBird flappyBird = new FlappyBird();
                frame.add(flappyBird);
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Background;
    private javax.swing.JLabel bottompipe;
    private javax.swing.JLabel flappybird;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel toppipe;
    // End of variables declaration//GEN-END:variables
}
