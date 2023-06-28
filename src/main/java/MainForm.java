import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainForm {
    private JPanel form;
    private JButton button1;



    public static void main(String[] args) {
        JFrame myFrame = new JFrame();
        JButton btn = new JButton("点击我");
        JTextField field = new JTextField(10); //JTextField(10)长度为10的文本输入框
        //使用Jpanel容器装载JButton和JTextFiedl组件
        JPanel jPanel = new JPanel();
        jPanel.add(btn);
        jPanel.add(field);
        myFrame.add(jPanel);
        //设置窗口的大小宽高都为300像素以及位置距离屏幕左上方向右300像素以及向下300像素
        myFrame.setBounds(300, 300, 300, 300);
        //必须设置这个属性 才可以看见窗口
        myFrame.setVisible(true);

        //为btn设置事件监听器 监听器采用匿名内部类的形式
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                field.setText("Button被点击了");
            }
        });

    }

}
