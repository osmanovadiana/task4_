package ru.vsu.cs.course1;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import ru.vsu.cs.util.ArrayUtils;
import ru.vsu.cs.util.JTableUtils;
import ru.vsu.cs.util.SwingUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class FrameMain extends JFrame {
    private static Font font = new Font(Font.SERIF, Font.PLAIN, 20);

    private enum ModeType {Init, Manual, Auto}

    Timer timer;
    private StateViewer viewer;
    private SortStateManager manager;

    private JPanel panelMain;
    private JTable tableInput;
    private JScrollPane scrollPaneTableInput;
    private JSlider stateSlider;
    private JButton prevButton;
    private JButton nextButton;
    private JTextField periodValue;
    private JLabel lab;
    private JCheckBox cycleCheckBox;
    private JButton startButton;
    private JButton resetButton;
    private JButton createArrayButton;
    private JSpinner arraySize;
    private JPanel resultPanel;
    private JPanel panelArray;
    private JPanel panelSlider;
    private JPanel panelPeriod;
    private JButton buttonExecute;
    private JScrollPane scrollPaneResult;


    private JMenuBar menuBarMain;
    private JMenu menuLookAndFeel;


    public FrameMain() {
        setTitle("FrameMain");
        setContentPane(panelMain);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();


        manager = new SortStateManager();
        JTableUtils.initJTableForArray(tableInput, 40, false, true, false, true);
        tableInput.setRowHeight(25);
        scrollPaneTableInput.setPreferredSize(new Dimension(-1, 90));
        JTableUtils.writeArrayToJTable(tableInput, new int[]{0, 1, 2, 3, 4, 5});
        menuBarMain = new JMenuBar();
        setJMenuBar(menuBarMain);
        menuLookAndFeel = new JMenu();
        menuLookAndFeel.setText("Вид");
        menuBarMain.add(menuLookAndFeel);
        SwingUtils.initLookAndFeelMenu(menuLookAndFeel);

        arraySize.setModel(new SpinnerNumberModel(10, 1, 30, 1));

        SortDrawPanel panel = new SortDrawPanel();
        viewer = panel;
        resultPanel.setLayout(new GridLayout());
        resultPanel.add(panel);
        resultPanel.repaint();
        this.pack();

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.next();
            }
        });
        createArrayButton.addActionListener(actionEvent -> {
            try {
                int[] arr = ArrayUtils.createRandomIntArray((Integer) arraySize.getValue(), 100);
                JTableUtils.writeArrayToJTable(tableInput, arr);
            } catch (Exception e) {
                SwingUtils.showErrorMessageBox(e);
            }
        });

        buttonExecute.addActionListener(actionEvent -> {
            try {
                int[] arr = JTableUtils.readIntArrayFromJTable(tableInput);
                manager.setStates(Sorting.BubbleSort(arr));
                setControlsEnabled(ModeType.Manual);
            } catch (Exception e) {
                SwingUtils.showErrorMessageBox(e);
            }
        });

        startButton.addActionListener(actionEvent -> {
            if (timer.isRunning()) {
                timer.stop();
                startButton.setText("Запустить");
                setControlsEnabled(ModeType.Manual);
            } else {
                int delay = 0;
                try {
                    delay = Integer.parseInt(periodValue.getText());
                } catch (NumberFormatException e) {
                    SwingUtils.showInfoMessageBox("Не удалось прочитать значение периода", "Ошибка");
                    e.printStackTrace();
                    return;
                }
                if (delay < 0) {
                    SwingUtils.showInfoMessageBox("Период не может быть отрицательным", "Ошибка");
                    return;
                }
                startButton.setText("Остановить");
                setControlsEnabled(ModeType.Auto);
                manager.setFlag(cycleCheckBox.isSelected());
                timer.setDelay(delay);
                timer.setInitialDelay(delay);
                timer.start();
            }
        });

        stateSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                manager.setCurrentIndex(stateSlider.getValue());
            }
        });

        manager.setListener(new SortStateManager.SortStateChangedListener() {
            @Override
            public void stateChanged(SortState state, int index, int total) {
                setTitle(String.format("%d%d ", index + 1, total));
                stateSlider.setMinimum(0);
                stateSlider.setMaximum(total - 1);
                stateSlider.setValue(index);
                viewer.show(state);
            }

            @Override
            public void finished() {
                timer.stop();
                startButton.setText("Запустить");
            }
        });

        nextButton.addActionListener(e -> manager.next());

        prevButton.addActionListener(e -> manager.prev());

        resetButton.addActionListener(e -> manager.reset());

    }

    public static void drawState(SortState ss, Graphics2D g, Point start) {
        int[] arr = ss.getArray();
        for (int i = 0; i < arr.length; i++) {
            Point p = new Point((int) (start.getX() + i * 50), (int) start.getY());
            Color c = Color.WHITE;
            int size = 40;
            if (i <= ss.getLeft() || i >= ss.getRight()) {
                c = Color.GRAY;
                size = 30;
            } else if (i == ss.getA() || i == ss.getB()) {
                if (ss.getType() == SortState.Type.Compare) {
                    c = Color.orange;
                    size = 50;
                } else
                    c = Color.RED;
            }
            drawItem(arr[i], g, p, c, size);
        }
    }

    public static void drawItem(int value, Graphics2D g, Point pos, Color color, int size) {
        g.setColor(color);
        g.fillOval((int) (pos.getX() - size / 2), (int) (pos.getY() - size / 2), size, size);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawOval((int) (pos.getX() - size / 2), (int) (pos.getY() - size / 2), size, size);
        g.setFont(font);
        String val = String.valueOf(value);
        int w = g.getFontMetrics().stringWidth(val);
        g.drawString(val, (int) pos.getX() - w / 2, (int) pos.getY() + 5);
    }

    private void setControlsEnabled(ModeType mode) {
        createArrayButton.setEnabled(mode != ModeType.Auto);
        tableInput.setEnabled(mode != ModeType.Auto);
        buttonExecute.setEnabled(mode != ModeType.Auto);
        startButton.setEnabled(mode != ModeType.Init);
        resetButton.setEnabled(mode == ModeType.Manual);
        cycleCheckBox.setEnabled(mode != ModeType.Manual);
        prevButton.setEnabled(mode == ModeType.Manual);
        nextButton.setEnabled(mode == ModeType.Manual);
        stateSlider.setEnabled(mode == ModeType.Manual);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(6, 1, new Insets(10, 10, 10, 10), 10, 10));
        panelMain.setMinimumSize(new Dimension(800, 407));
        panelMain.setPreferredSize(new Dimension(800, 407));
        scrollPaneTableInput = new JScrollPane();
        scrollPaneTableInput.setVerticalScrollBarPolicy(21);
        panelMain.add(scrollPaneTableInput, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 85), null, 0, false));
        tableInput = new JTable();
        tableInput.setPreferredSize(new Dimension(150, 50));
        scrollPaneTableInput.setViewportView(tableInput);
        panelPeriod = new JPanel();
        panelPeriod.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(panelPeriod, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 40), new Dimension(-1, 40), new Dimension(-1, 40), 0, false));
        lab = new JLabel();
        lab.setText("Период (мс)  ");
        panelPeriod.add(lab, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cycleCheckBox = new JCheckBox();
        cycleCheckBox.setText("циклично");
        panelPeriod.add(cycleCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startButton = new JButton();
        startButton.setText("заупск");
        panelPeriod.add(startButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resetButton = new JButton();
        resetButton.setText("сброс");
        panelPeriod.add(resetButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        periodValue = new JTextField();
        periodValue.setText("500");
        periodValue.setToolTipText("500");
        panelPeriod.add(periodValue, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        panelSlider = new JPanel();
        panelSlider.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(panelSlider, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stateSlider = new JSlider();
        panelSlider.add(stateSlider, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        prevButton = new JButton();
        prevButton.setText("<");
        panelSlider.add(prevButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextButton = new JButton();
        nextButton.setText(">");
        panelSlider.add(nextButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelArray = new JPanel();
        panelArray.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:grow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
        panelMain.add(panelArray, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 40), new Dimension(-1, 40), new Dimension(-1, 40), 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Создать массив из ");
        CellConstraints cc = new CellConstraints();
        panelArray.add(label1, cc.xywh(1, 1, 1, 3));
        createArrayButton = new JButton();
        createArrayButton.setText("создать");
        panelArray.add(createArrayButton, cc.xywh(7, 1, 1, 3));
        arraySize = new JSpinner();
        panelArray.add(arraySize, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label2 = new JLabel();
        label2.setText("элементов");
        panelArray.add(label2, cc.xywh(5, 1, 1, 3));
        final Spacer spacer1 = new Spacer();
        panelArray.add(spacer1, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        scrollPaneResult = new JScrollPane();
        scrollPaneResult.setHorizontalScrollBarPolicy(30);
        scrollPaneResult.setVerticalScrollBarPolicy(21);
        panelMain.add(scrollPaneResult, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 80), null, 0, false));
        resultPanel = new JPanel();
        resultPanel.setLayout(new FormLayout("", ""));
        scrollPaneResult.setViewportView(resultPanel);
        buttonExecute = new JButton();
        buttonExecute.setText("сортировать");
        panelMain.add(buttonExecute, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

class SortDrawPanel extends JPanel implements StateViewer{
    private SortState state = null;

    @Override
    public void show(SortState ss) {
        state = ss;
        if(state != null)
            this.setMinimumSize(new Dimension(state.getArray().length * 50 + 200, 200));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(state == null)
            return;
        Graphics2D gr = (Graphics2D)g;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FrameMain.drawState(state, gr, new Point(30, 30));
    }
}