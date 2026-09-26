package io.blockdesigner.refplanes;

import io.blockdesigner.plugin.ObjectHandle;
import io.blockdesigner.plugin.Pose;
import io.blockdesigner.plugin.ToolEvent.Vec3;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Every number of a reference image in one window: position, rotation, scale, UV offset and scale, opacity. Apply
 * changes the pose and the settings (an undo step each, when they changed).
 */
final class PropertiesDialog {
    private PropertiesDialog() {
    }

    static void show(ObjectHandle self, ReferenceImage ref) {
        Pose p = self.pose();
        ReferenceSettings s = ref.settings();
        GridPane g = ReferenceMenu.grid();
        g.setHgap(8);
        g.setVgap(6);
        String[] heads = {"X", "Y", "Z"};
        for (int i = 0; i < 3; i++) {
            Label h = new Label(heads[i]);
            h.setStyle("-fx-text-fill: -color-fg-muted;");
            g.add(h, i + 1, 0);
        }
        TextField[] pose = new TextField[9];
        double[] v = {p.position().x(), p.position().y(), p.position().z(), p.rotation().x(), p.rotation().y(), p.rotation().z(),
                p.scale().x(), p.scale().y(), p.scale().z()};
        String[] rows = {"Position (blocks)", "Rotation (°)", "Scale"};
        for (int r = 0; r < 3; r++) {
            g.add(new Label(rows[r]), 0, r + 1);
            for (int c = 0; c < 3; c++) g.add(pose[r * 3 + c] = ReferenceMenu.field(v[r * 3 + c]), c + 1, r + 1);
        }
        TextField[] uv = {ReferenceMenu.field(s.uOffset()), ReferenceMenu.field(s.vOffset()), ReferenceMenu.field(s.uScale()),
                ReferenceMenu.field(s.vScale())};
        g.add(new Label("UV offset (U, V)"), 0, 4);
        g.add(uv[0], 1, 4);
        g.add(uv[1], 2, 4);
        g.add(new Label("UV scale (U, V)"), 0, 5);
        g.add(uv[2], 1, 5);
        g.add(uv[3], 2, 5);
        TextField[] opacity = {ReferenceMenu.field(Math.round(s.opacity() * 1000) / 10.0)};
        g.add(new Label("Opacity (%)"), 0, 6);
        g.add(opacity[0], 1, 6);
        Label note = new Label(s.fileName() + " · " + s.width() + "×" + s.height() + " px · 1 block tall at scale 1");
        note.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        g.add(note, 0, 7, 4, 1);

        Dialog<ButtonType> d = new Dialog<>();
        d.initOwner(ReferenceType.owner());
        d.setTitle(self.name());
        d.setHeaderText(self.name() + " · reference image");
        d.getDialogPane().setContent(g);
        ButtonType apply = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().setAll(apply, ButtonType.CANCEL);
        // Keep the window open while a number is wrong.
        d.getDialogPane().lookupButton(apply).addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            boolean bad = ReferenceMenu.parse(pose) == null;
            bad |= ReferenceMenu.parse(uv) == null;
            bad |= ReferenceMenu.parse(opacity) == null;
            if (bad) e.consume();
        });
        if (d.showAndWait().orElse(ButtonType.CANCEL) != apply || !self.exists()) return;
        double[] n = ReferenceMenu.parse(pose), u = ReferenceMenu.parse(uv), o = ReferenceMenu.parse(opacity);
        self.setPose(new Pose(new Vec3(n[0], n[1], n[2]), new Vec3(n[3], n[4], n[5]), new Vec3(n[6], n[7], n[8])),
                "Edit transform of " + self.name());
        ReferenceSettings want = ref.settings().withUv(u[0], u[1], u[2], u[3]).withOpacity(o[0] / 100);
        if (!want.equals(ref.settings())) ref.change(self, "Edit " + self.name(), x -> want);
    }
}
