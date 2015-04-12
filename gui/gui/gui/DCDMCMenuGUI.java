package gui;

import starter.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Project: DCDMC
 * Package: gui
 * Date: 11/Apr/2015
 * Time: 23:12
 * System Time: 11:12 PM
 */
public class DCDMCMenuGUI {
    private static final Logger LOGGER = Logger.getLogger(DCDMCMenuGUI.class.getName());

    private final DCDMCGUI dcdmcgui;

    public DCDMCMenuGUI(DCDMCGUI dcdmcgui) {
        this.dcdmcgui = dcdmcgui;
    }

    /**
     * Create menu bar
     * @return a object of JMenuBar
     * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/uiswing/examples/components/MenuDemoProject/src/components/MenuDemo.java
     */
    public JMenuBar createMenuBar() {

        JMenuBar menuBar;
        JMenu menu;
        JMenu submenu;
        JMenuItem menuItem;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //---------------------------------------- File Menu ---------------------------------------//
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menu);

        //Import Config File
        menuItem = new JMenuItem("Import Config File ...", KeyEvent.VK_I);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create a file chooser

                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(DCDMCMenuGUI.this.dcdmcgui);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("        Open File: [" + file.getAbsolutePath() + "] successfully.");
                    DCDMCMenuGUI.this.dcdmcgui.importConfigFile(file);


                } else {
                    System.out.println("        Cancel to open file.");
                }
            }
        });

        // Export Config File
        menuItem = new JMenuItem("Export Config File ...", KeyEvent.VK_E);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Create a file chooser

                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showSaveDialog(DCDMCMenuGUI.this.dcdmcgui);

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = fc.getSelectedFile();
                    DCDMCMenuGUI.this.dcdmcgui.exportConfigFile(file);
                    System.out.println("        Save File: [" + file.getAbsolutePath() + "] successfully.");

                } else {
                    System.out.println("        Cancel to save file.");
                }
            }
        });

        menu.addSeparator();

        //a submenu

        submenu = new JMenu("Settings...");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("Paths...");
        submenu.add(menuItem);
        menu.add(submenu);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DCDMCMenuGUI.this.dcdmcgui.setAllComponentsEnabled(false);
                PathSettingsGUI pathSettingsGUI = new PathSettingsGUI(DCDMCMenuGUI.this.dcdmcgui);
            }
        });

        menu.addSeparator();

        menuItem = new JMenuItem("Exit", KeyEvent.VK_0);
        menuItem.addActionListener(
                new ActionListener() {
                    /**
                     * Action performed function
                     * @param e event
                     */
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // exit the application when window's close button is clicked
                        System.exit(0);
                    }
                }

        );
        menu.add(menuItem);

        //-------------------------------------- Settings Menu --------------------------------------//
        menu = new JMenu("Edit");
        menuBar.add(menu);
        menuItem = new JMenuItem("Reset Configurations");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DCDMCMenuGUI.this.dcdmcgui.setAllComponentsEnabled(false);
                Object[] options = {"Yes, please",
                        "No, thanks"};
                int n = JOptionPane.showOptionDialog(DCDMCMenuGUI.this.dcdmcgui,
                        "Would you like to reset all configurations?",
                        "Caution",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[1]);

                // press yes button
                if (n == 0) {
                    Config.resetConfig(); // reset all parameters
                    DCDMCMenuGUI.this.dcdmcgui.resetAllComponents(); // reset all gui configuration
                    System.out.println("            Reset configurations in all components successfully.");
                } else {
                    System.out.println("            Cancel to reset configurations in all components.");
                }

                DCDMCMenuGUI.this.dcdmcgui.setAllComponentsEnabled(true);
            }
        });

        menuItem = new JMenuItem("Show All Configurations");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame frame = new JFrame("Distributed Collective Dynamical Modeling & Clustering Configurations");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                //Create the content-pane-to-be.
                JPanel contentPane = new JPanel(new BorderLayout());
                contentPane.setOpaque(true);

                //Create a scrolled text area.
                JTextArea output = new JTextArea(28, 75);
                output.setEditable(false);
                output.append("\n");
                Config config = new Config(DCDMCMenuGUI.this.dcdmcgui.getParameters());
                output.append(Config.toFormatAsString());
                output.setCaretPosition(output.getDocument().getLength());
                JScrollPane scrollPane = new JScrollPane(output);

                //Add the text area to the content pane.
                contentPane.add(scrollPane, BorderLayout.CENTER);

                // Add the button to the content pane.
                JButton jButton = new JButton("Close");
                jButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame.dispose();
                    }
                });
                contentPane.add(jButton, BorderLayout.PAGE_END);

                //Create and set up the content pane.
                frame.setContentPane(contentPane);

                //Display the window.
                Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
                frame.setSize((int)screenDimension.getWidth() / 2, (int)screenDimension.getWidth() / 2);
                frame.setLocationRelativeTo(null);
                frame.pack();
                frame.setVisible(true);
            }
        });

        //---------------------------------------- Help Menu ----------------------------------------//
        menu = new JMenu("Help");
        menuBar.add(menu);

        menuItem = new JMenuItem("Software website");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI("https://github.com/wcy1984123/DCDMCS"));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        menuItem = new JMenuItem("Software Version & Copyright");
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DCDMCMenuGUI.this.dcdmcgui, Config.getVERSIONINFO(), "Software Version & Copyright Information", JOptionPane.WARNING_MESSAGE);
            }
        });


        menuItem = new JMenuItem("About us");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DCDMCMenuGUI.this.dcdmcgui, "KNOWLEDGE DISCOVERY AND DATA MINING \n" +
                        "RESEARCH GROUP", "About us", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(menuItem);

        return menuBar;
    }

    /**
     * test
     * @param args user input
     */
    public static void main(String[] args) {

    }
}