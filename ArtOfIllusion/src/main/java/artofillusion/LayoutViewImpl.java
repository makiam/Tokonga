package artofillusion;

import artofillusion.ui.EditingTool;
import artofillusion.ui.EditingWindow;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

@Slf4j
class LayoutViewImpl extends JFrame implements GLEventListener {

    private EditingWindow owner;
    private GLJPanel canvas;

    public LayoutViewImpl(EditingWindow owner) throws HeadlessException {
        this.owner = owner;
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setTitle("LayoutNew");
        this.setSize(1280, 1024);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(owner.confirmClose()) {
                    LayoutViewImpl.this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                }
                super.windowClosing(e);
            }
        });
        this.getContentPane().add(canvas = new GLJPanel(AppGLCapabilities.INSTANCE.getCapabilities()));
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, canvas.getChosenGLCapabilities(), "LayoutNew", JOptionPane.INFORMATION_MESSAGE));
    }

    public void setHelpText(String text) {

    }

    public void setTool(EditingTool tool) {

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    @Subscribe
    public void onEvent(org.greenrobot.eventbus.NoSubscriberEvent nop) {
    }
}
