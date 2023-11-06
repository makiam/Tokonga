package artofillusion;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
/*  w ww  .j a v  a2  s . c o  m*/
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public class Main {
  public static void main(String[] args) throws Exception {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    MyTreeNode root = new MyTreeNode(1, 0);
    DefaultTreeModel model = new DefaultTreeModel(root);
    JProgressBar bar = new JProgressBar();
    PropertyChangeListener progressListener = (PropertyChangeEvent evt) -> {
        bar.setValue((Integer) evt.getNewValue());
    };
    JTree tree = new JTree();
    tree.setShowsRootHandles(true);
    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      @Override
      public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
        TreePath path = event.getPath();
        if (path.getLastPathComponent() instanceof MyTreeNode) {
          MyTreeNode node = (MyTreeNode) path.getLastPathComponent();
          node.loadChildren(model, progressListener);
        }
      }
      @Override
      public void treeWillCollapse(TreeExpansionEvent event)
          throws ExpandVetoException {

      }
    });
    tree.setModel(model);
    root.loadChildren(model, progressListener);
    frame.add(new JScrollPane(tree));
    frame.add(bar, BorderLayout.SOUTH);
    frame.pack();
    frame.setVisible(true);
  }
}

class MyTreeNode extends DefaultMutableTreeNode {
  boolean loaded = false;
  int depth;
  int index;

  public MyTreeNode(int index, int depth) {
    this.index = index;
    this.depth = depth;
    add(new DefaultMutableTreeNode("Loading...", false));
    setAllowsChildren(true);
    setUserObject("Index " + index + " at level " + depth);
  }

  private void setChildren(List<MyTreeNode> children) {
    removeAllChildren();
    setAllowsChildren(children.size() > 0);
    for (MutableTreeNode node : children) {
      add(node);
    }
    loaded = true;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  public void loadChildren(final DefaultTreeModel model,final PropertyChangeListener progressListener) {
    if (loaded) {
      return;
    }
    SwingWorker<List<MyTreeNode>, Void> worker = new SwingWorker<List<MyTreeNode>, Void>() {
      @Override
      protected List<MyTreeNode> doInBackground() throws Exception {
        setProgress(0);
        List<MyTreeNode> children = new ArrayList<>();
        if (depth < 5) {
          for (int i = 0; i < 5; i++) {
            Thread.sleep(300);
            children.add(new MyTreeNode(i + 1, depth + 1));
            setProgress((i + 1) * 20);
          }
        } else {
          Thread.sleep(1000);
        }
        setProgress(0);
        return children;
      }

      @Override
      protected void done() {
        try {
          setChildren(get());
          model.nodeStructureChanged(MyTreeNode.this);
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
        super.done();
      }
    };
    if (progressListener != null) {
      worker.getPropertyChangeSupport().addPropertyChangeListener("progress",progressListener);
    }
    worker.execute();
  }
}


