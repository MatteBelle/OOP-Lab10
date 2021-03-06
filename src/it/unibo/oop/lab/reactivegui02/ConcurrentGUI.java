package it.unibo.oop.lab.reactivegui02;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class ConcurrentGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    /*
     * Realizzare una classe ConcurrentGUI con costruttore privo di argomenti,
     * tale che quando istanziata crei un JFrame con l'aspetto mostrato nella
     * figura allegata (e contatore inizialmente posto a zero).
     * 
     * Il contatore venga aggiornato incrementandolo ogni 100 millisecondi
     * circa, e il suo nuovo valore venga mostrato ogni volta (l'interfaccia sia
     * quindi reattiva).
     * 
     * Alla pressione del pulsante "down", il conteggio venga da lì in poi
     * aggiornato decrementandolo; alla pressione del pulsante "up", il
     * conteggio venga da lì in poi aggiornato incrementandolo; e così via, in
     * modo alternato.
     * 
     * Alla pressione del pulsante "stop", il conteggio si blocchi, e i tre
     * pulsanti vengano disabilitati. Per far partire l'applicazioni si tolga il
     * commento nel main qui sotto.
     * 
     * Suggerimenti: - si mantenga la struttura dell'esercizio precedente - per
     * pilotare la direzione su/giù si aggiunga un flag booleano all'agente --
     * deve essere volatile? - la disabilitazione dei pulsanti sia realizzata
     * col metodo setEnabled
     */
    public ConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();
        new Thread(agent).start();

        up.addActionListener(e -> agent.choose = true);
        down.addActionListener(e -> agent.choose = false);
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.stopCounting();
                up.setEnabled(false);
                down.setEnabled(false);
                stop.setEnabled(false);
            }
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop;
        private volatile boolean choose = true;
        private volatile int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(Integer.toString(Agent.this.counter)));
                    if (choose) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }
    }
}
