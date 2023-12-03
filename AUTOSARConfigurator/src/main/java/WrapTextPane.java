
import javax.swing.*;
import javax.swing.text.*;

public class WrapTextPane extends JTextPane {
    public WrapTextPane() {
        this.setEditorKit(new WrapEditorKit());
    }

    private static class WrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory = new WrapColumnFactory();
        
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    private static class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                }
            }

            // default to text display
            return new LabelView(elem);
        }
    }

    private static class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        public float getMinimumSpan(int axis) {
            switch (axis) {
                case View.X_AXIS:
                    return 0;
                case View.Y_AXIS:
                    return super.getMinimumSpan(axis);
                default:
                    throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }
    }
}
