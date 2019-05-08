import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.atlassian.jira.rest.client.api.domain.Issue;
import net.sf.clipsrules.jni.*;

/* TBD module qualifier with find-all-facts */

/*

 Notes:

 This example creates just a single environment. If you create multiple environments,
 call the destroy method when you no longer need the environment. This will free the
 C data structures associated with the environment.

 clips = new Environment();
 .
 .
 .
 clips.destroy();

 Calling the clear, reset, load, loadFacts, run, eval, build, assertString,
 and makeInstance methods can trigger CLIPS garbage collection. If you need
 to retain access to a PrimitiveValue returned by a prior eval, assertString,
 or makeInstance call, retain it and then release it after the call is made.

 PrimitiveValue pv1 = clips.eval("(myFunction foo)");
 pv1.retain();
 PrimitiveValue pv2 = clips.eval("(myFunction bar)");
 .
 .
 .
 pv1.release();

 */

public class WineDemo{
    JFrame jfrm;

    DefaultTableModel wineList;

    public int  procent =0;

    public String ticket = "";
    JLabel jlab;

    ResourceBundle wineResources;

    Environment clips;

    boolean isExecuting = false;
    Thread executionThread;

    class WeightCellRenderer extends JProgressBar implements TableCellRenderer {
        public WeightCellRenderer() {
            super(JProgressBar.HORIZONTAL, 0, 100);
            setStringPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            setValue(((Number) value).intValue());
            return WeightCellRenderer.this;
        }
    }

    /************/
    /* WineDemo */
    /***********/
    WineDemo(String ticket) throws  FileNotFoundException {
        this.ticket=ticket;

        try {
            this.wineResources = ResourceBundle.getBundle("properties.WineResources",
                    Locale.getDefault());
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            return;
        }


//        try {
//            new JRC().createIssue();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        /* =================================== */
        /* Create a new JFrame container and */
        /* assign a layout manager to it. */
        /* =================================== */

        this.jfrm = new JFrame(wineResources.getString("WineDemo"));
        this.jfrm.getContentPane().setLayout(
                new BoxLayout(this.jfrm.getContentPane(), BoxLayout.Y_AXIS));

        /* ================================= */
        /* Give the frame an initial size. */
        /* ================================= */

        this.jfrm.setSize(480, 390);

        /* ============================================================= */
        /* Terminate the program when the user closes the application. */
        /* ============================================================= */

        this.jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        /* ============================================== */
        /* Create a panel including the preferences and */
        /* meal panels and add it to the content pane. */
        /* ============================================== */

        final JPanel choicesPanel = new JPanel();
        choicesPanel.setLayout(new FlowLayout());
        JTextArea text = new JTextArea("",5,30);
        choicesPanel.add(new JScrollPane(text));

        JButton buttText = new JButton("Submit");
        choicesPanel.add(new JScrollPane(buttText));


        buttText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        this.jfrm.getContentPane().add(choicesPanel);

        /* ================================== */
        /* Create the recommendation panel. */
        /* ================================== */

        this.wineList = new DefaultTableModel();

        this.wineList.setDataVector(new Object[][] {},
                new Object[] { this.wineResources.getString("WineTitle"),
                        this.wineResources.getString("RecommendationTitle") });

        final JTable table = new JTable(this.wineList) {
            public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
            }
        };

        table.setCellSelectionEnabled(false);

        final WeightCellRenderer renderer = this.new WeightCellRenderer();
        renderer.setBackground(table.getBackground());

        table.getColumnModel().getColumn(1).setCellRenderer(renderer);

        final JScrollPane pane = new JScrollPane(table);

        table.setPreferredScrollableViewportSize(new Dimension(450, 210));

        /* =================================================== */
        /* Add the recommendation panel to the content pane. */
        /* =================================================== */

        this.jfrm.getContentPane().add(pane);


        /* ======================== */
        /* Load the wine program. */
        /* ======================== */

        this.clips = new Environment();

        this.clips.load("winedemo.clp") ;

        try {
            runWine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* ==================== */
        /* Display the frame. */
        /* ==================== */

        this.jfrm.pack();
     //   this.jfrm.setVisible(true);

//        String jiraKey = "WAR-5";
//        Issue issue = null;
//        try {
//            issue = new JRC().getIssue(jiraKey);
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//        //ticket = issue.getSummary();
        ticket = text.getText();
//        System.out.println(ticket);
//        try {
//            runWine();
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }

    }



    /* ######################## */
    /* ActionListener Methods */
    /* ######################## */

    /*******************/
    /* actionPerformed */
    /*******************/
//    public void actionPerformed(ActionEvent ae) {
//        if (this.clips == null)
//            return;
//
//        try {
//            runWine();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /***********/
    /* runWine */
    /***********/
    public void runWine() throws Exception {
        String item;
        this.clips = new Environment();

        this.clips.load("winedemo.clp") ;

        if (this.isExecuting)
            return;

        this.clips.reset();


        if (ticket.contains("opona")) {
            this.clips.assertString("(attribute (name preferred-color) (value opona))");
        } else if (ticket.contains("sprzeglo")) {
            this.clips.assertString("(attribute (name preferred-color) (value sprzeglo))");
        } else {
            this.clips.assertString("(attribute (name preferred-color) (value unknown))");
        }


        if (ticket.contains("silnik")) {
            this.clips.assertString("(attribute (name preferred-body) (value silnik))");
        } else if (ticket.contains("reflektor")) {
            this.clips.assertString("(attribute (name preferred-body) (value reflektor))");
        } else if (ticket.contains("hamulce")) {
            this.clips.assertString("(attribute (name preferred-body) (value hamulce))");
        } else {
            this.clips.assertString("(attribute (name preferred-body) (value unknown))");
        }


        if (ticket.contains("hamulce")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value hamulce))");
        } else if (ticket.contains("reflektor")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value reflektor))");
        } else if (ticket.contains("klocki")) {
            this.clips.assertString("(attribute (name preferred-sweetness) (value klocki))");
        } else {
            this.clips.assertString("(attribute (name preferred-sweetness) (value unknown))");
        }


        if (ticket.contains("uklad") || ticket.contains("opona") || ticket.contains("elektryka")) {
            this.clips.assertString("(attribute (name main-component) (value meat))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else if (ticket.contains("silnik")) {
            this.clips.assertString("(attribute (name main-component) (value poultry))");
            this.clips.assertString("(attribute (name has-turkey) (value yes))");
        } else if (ticket.contains("hamulce") || ticket.contains("Duck")) {
            this.clips.assertString("(attribute (name main-component) (value poultry))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else if (ticket.contains("klocki")) {
            this.clips.assertString("(attribute (name main-component) (value klocki))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else if (ticket.contains("sprzeglo")) {
            this.clips.assertString("(attribute (name main-component) (value unknown))");
            this.clips.assertString("(attribute (name has-turkey) (value no))");
        } else {
            this.clips.assertString("(attribute (name main-component) (value unknown))");
            this.clips.assertString("(attribute (name has-turkey) (value unknown))");
        }


        if (ticket.contains("klimatyzacja")) {
            this.clips.assertString("(attribute (name has-sauce) (value no))");
        } else if (ticket.contains("skrzynia")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value skrzynia))");
        } else if (ticket.contains("uklad")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value klocki))");
        } else if (ticket.contains("elektryka")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value elektryka))");
        } else if (ticket.contains("sprzeglo")) {
            this.clips.assertString("(attribute (name has-sauce) (value yes))");
            this.clips.assertString("(attribute (name sauce) (value unknown))");
        } else {
            this.clips.assertString("(attribute (name has-sauce) (value unknown))");
            this.clips.assertString("(attribute (name sauce) (value unknown))");
        }


        if (ticket.contains("opona")) {
            this.clips.assertString("(attribute (name tastiness) (value opona))");
        } else if (ticket.contains("uklad")) {
            this.clips.assertString("(attribute (name tastiness) (value uklad))");
        } else if (ticket.contains("skrzynia")) {
            this.clips.assertString("(attribute (name tastiness) (value skrzynia))");
        } else {
            this.clips.assertString("(attribute (name tastiness) (value unknown))");
        }

   //     final Runnable runThread = new Runnable() {
         //   public void run() {
                clips.run();

                //swingUtilities.invokeLater(new Runnable() {
                  //  public void run() {



        this.isExecuting = true;

        //this.executionThread = new Thread(runThread);

        //this.executionThread.start();
    }

    /***************/
    /* updateWines */
    /***************/
    // It isn't necessary to explicitly throw the ClassCastException,
    // but I wrote it to make clear that the castings might not always be right.
    // It depends on the template declarations, which in this case match with the expected value types.
    public String updateWines() throws ClassCastException{
        final String evalStr = "(WINES::get-wine-list)";
        final MultifieldValue pv = (MultifieldValue) this.clips.eval(evalStr);
        this.wineList.setRowCount(0);
        String kto ="";
        try {
            for (int i = 0; i < pv.size(); i++) {
                final FactAddressValue fv = (FactAddressValue) pv.get(i);
                final int certainty;
                certainty = (int) ((FloatValue) fv.getFactSlot("certainty")).floatValue();
                if (procent< certainty) {
                    kto  = ((StringValue) fv.getFactSlot("value")).stringValue();
                    procent = certainty;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(procent);
        this.wineList.addRow(new Object[] {kto , new Integer(procent) });

        this.jfrm.pack();

        this.executionThread = null;

        this.isExecuting = false;

        System.out.println(kto);
        return kto;
    }



    /********/
    /* main */
    /********/
}
