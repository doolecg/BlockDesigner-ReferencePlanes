package io.blockdesigner.refplanes;

import io.blockdesigner.plugin.Drawing;
import io.blockdesigner.plugin.ObjectHandle;
import io.blockdesigner.plugin.Pose;
import io.blockdesigner.plugin.ToolEvent.Vec3;
import io.blockdesigner.plugin.ViewInfo;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A reference image's own right-click entries: transform and UV fields, opacity, the views it shows in, how blocks
 * cover it, flips, aligning to the view, resets and replacing the picture. BlockDesigner adds rename, focus, hide,
 * lock and delete after them.
 */
final class ReferenceMenu {
    private static final Map<Drawing.Depth, String> DEPTHS = Map.of(Drawing.Depth.BEHIND_BLOCKS, "Behind blocks",
            Drawing.Depth.IN_SCENE, "In the scene (blocks in front hide it)", Drawing.Depth.IN_FRONT, "In front of blocks");

    private ReferenceMenu() {
    }

    static List<MenuItem> items(ObjectHandle self, ReferenceImage ref, ReferenceType type) {
        ReferenceSettings s = ref.settings();
        List<MenuItem> out = new ArrayList<>();

        Menu transform = new Menu("Position, rotation, scale");
        transform.getItems().add(fields(poseGrid(self)));
        MenuItem resetRotation = new MenuItem("Reset rotation");
        resetRotation.setOnAction(e -> self.setPose(self.pose().withRotation(new Vec3(0, 0, 0)), "Reset rotation of " + self.name()));
        MenuItem resetAspect = new MenuItem("Reset aspect ratio (the picture's own)");
        resetAspect.setOnAction(e -> self.setPose(self.pose().withScale(ReferenceSettings.resetAspect(self.pose().scale())),
                "Reset aspect of " + self.name()));
        transform.getItems().addAll(new SeparatorMenuItem(), resetRotation, resetAspect);

        Menu uv = new Menu("UV offset and scale");
        uv.getItems().add(fields(uvGrid(self, ref)));
        MenuItem resetUv = new MenuItem("Reset UV");
        resetUv.setOnAction(e -> ref.change(self, "Reset UV of " + self.name(), x -> x.withUv(0, 0, 1, 1)));
        uv.getItems().addAll(new SeparatorMenuItem(), resetUv);

        Menu opacity = new Menu("Opacity · " + Math.round(s.opacity() * 100) + "%");
        opacity.getItems().add(fields(opacitySlider(self, ref)));
        opacity.getItems().add(new SeparatorMenuItem());
        ToggleGroup og = new ToggleGroup();
        for (int pct : new int[]{100, 75, 50, 25}) {
            RadioMenuItem r = new RadioMenuItem(pct + "%");
            r.setToggleGroup(og);
            r.setSelected(Math.round(s.opacity() * 100) == pct);
            r.setOnAction(e -> ref.change(self, "Opacity " + pct + "% for " + self.name(), x -> x.withOpacity(pct / 100.0)));
            opacity.getItems().add(r);
        }

        Menu showIn = new Menu("Show in · " + s.showIn().label);
        ToggleGroup sg = new ToggleGroup();
        for (ReferenceSettings.ShowIn v : ReferenceSettings.ShowIn.values()) {
            RadioMenuItem r = new RadioMenuItem(v.label + (v.side().isPresent() ? " (orthographic)" : ""));
            r.setToggleGroup(sg);
            r.setSelected(s.showIn() == v);
            r.setOnAction(e -> ref.change(self, "Show " + self.name() + " in " + v.label.toLowerCase(Locale.ROOT), x -> x.withShowIn(v)));
            showIn.getItems().add(r);
            if (v == ReferenceSettings.ShowIn.ORTHO) showIn.getItems().add(new SeparatorMenuItem());
        }

        Menu depth = new Menu("Draw");
        ToggleGroup dg = new ToggleGroup();
        for (Drawing.Depth d : Drawing.Depth.values()) {
            RadioMenuItem r = new RadioMenuItem(DEPTHS.get(d));
            r.setToggleGroup(dg);
            r.setSelected(s.depth() == d);
            r.setOnAction(e -> ref.change(self, "Draw " + self.name() + " " + DEPTHS.get(d).toLowerCase(Locale.ROOT), x -> x.withDepth(d)));
            depth.getItems().add(r);
        }

        CheckMenuItem flipX = new CheckMenuItem("Flip horizontally");
        flipX.setSelected(s.flipX());
        flipX.setOnAction(e -> ref.change(self, "Flip " + self.name(), x -> x.withFlip(!x.flipX(), x.flipY())));
        CheckMenuItem flipY = new CheckMenuItem("Flip vertically");
        flipY.setSelected(s.flipY());
        flipY.setOnAction(e -> ref.change(self, "Flip " + self.name(), x -> x.withFlip(x.flipX(), !x.flipY())));

        ViewInfo view = type.view();
        MenuItem align = new MenuItem(view.side().map(v -> "Align to the " + v.name().toLowerCase(Locale.ROOT) + " view").orElse("Face the camera"));
        align.setOnAction(e -> self.setPose(self.pose().withRotation(view.side().map(ViewInfo.Side::facing)
                .orElseGet(() -> ReferenceSettings.facingYaw(view.forward()))), "Align " + self.name() + " to the view"));
        MenuItem onlyHere = new MenuItem(view.side().map(v -> "Show only in the " + v.name().toLowerCase(Locale.ROOT) + " view").orElse(""));
        onlyHere.setOnAction(e -> ref.change(self, "Show " + self.name() + " in one view",
                x -> x.withShowIn(ReferenceSettings.ShowIn.of(view.side().orElseThrow()))));

        MenuItem replace = new MenuItem("Replace picture…");
        replace.setOnAction(e -> replace(self, ref, type));
        MenuItem props = new MenuItem("Properties…");
        props.setOnAction(e -> PropertiesDialog.show(self, ref));

        out.addAll(List.of(props, new SeparatorMenuItem(), transform, uv, opacity, showIn, depth, flipX, flipY, new SeparatorMenuItem(), align));
        if (view.side().isPresent()) out.add(onlyHere);
        out.add(replace);
        return out;
    }

    private static void replace(ObjectHandle self, ReferenceImage ref, ReferenceType type) {
        File f = ReferenceType.chooser("Replace the picture of " + self.name()).showOpenDialog(ReferenceType.owner());
        if (f == null) return;
        try {
            ReferenceType.Loaded l = type.load(f.toPath());
            ref.change(self, "Replace picture of " + self.name(), x -> x.withImage(l.blob(), l.fileName(), l.decoded().width(), l.decoded().height()));
            ref.set(ref.settings(), l.decoded().image());
        } catch (IOException ex) {
            type.toast("✖ " + f.getName() + ": " + ex.getMessage());
        }
    }

    /** A control inside the menu: clicking it doesn't close the menu. */
    private static CustomMenuItem fields(javafx.scene.Node content) {
        CustomMenuItem item = new CustomMenuItem(content, false);
        return item;
    }

    /** Position, rotation and scale fields; Enter (or leaving a field) applies them as one undo step. */
    static GridPane poseGrid(ObjectHandle self) {
        GridPane g = grid();
        Pose p = self.pose();
        TextField[] f = new TextField[9];
        double[] v = {p.position().x(), p.position().y(), p.position().z(), p.rotation().x(), p.rotation().y(), p.rotation().z(),
                p.scale().x(), p.scale().y(), p.scale().z()};
        String[] rows = {"Position", "Rotation °", "Scale"};
        for (int r = 0; r < 3; r++) {
            g.add(new Label(rows[r]), 0, r + 1);
            for (int c = 0; c < 3; c++) {
                f[r * 3 + c] = field(v[r * 3 + c]);
                g.add(f[r * 3 + c], c + 1, r + 1);
            }
        }
        axisHeader(g, "X", "Y", "Z");
        onCommit(f, () -> {
            double[] n = parse(f);
            if (n == null) return;
            Pose np = new Pose(new Vec3(n[0], n[1], n[2]), new Vec3(n[3], n[4], n[5]), new Vec3(n[6], n[7], n[8]));
            self.setPose(np, "Edit transform of " + self.name());
        });
        return g;
    }

    /** UV offset and scale fields (in picture widths and heights). */
    static GridPane uvGrid(ObjectHandle self, ReferenceImage ref) {
        GridPane g = grid();
        ReferenceSettings s = ref.settings();
        TextField[] f = {field(s.uOffset()), field(s.vOffset()), field(s.uScale()), field(s.vScale())};
        g.add(new Label("Offset"), 0, 1);
        g.add(f[0], 1, 1);
        g.add(f[1], 2, 1);
        g.add(new Label("Scale"), 0, 2);
        g.add(f[2], 1, 2);
        g.add(f[3], 2, 2);
        axisHeader(g, "U", "V");
        onCommit(f, () -> {
            double[] n = parse(f);
            if (n != null) ref.change(self, "Change UV of " + self.name(), x -> x.withUv(n[0], n[1], n[2], n[3]));
        });
        return g;
    }

    /** An opacity slider: the view follows while dragging; letting go makes one undo step. */
    private static HBox opacitySlider(ObjectHandle self, ReferenceImage ref) {
        Slider slider = new Slider(0, 100, ref.settings().opacity() * 100);
        slider.setPrefWidth(160);
        Label pct = new Label(Math.round(slider.getValue()) + "%");
        pct.setMinWidth(40);
        ReferenceSettings[] before = {null};
        Consumer<Double> commit = value -> {
            if (before[0] != null) {
                ref.set(before[0], null);
                before[0] = null;
            }
            if (Math.abs(ref.settings().opacity() * 100 - value) > 0.5) {
                ref.change(self, "Opacity " + Math.round(value) + "% for " + self.name(), x -> x.withOpacity(value / 100));
            }
        };
        slider.valueProperty().addListener((o, a, b) -> {
            pct.setText(Math.round(b.doubleValue()) + "%");
            if (slider.isValueChanging()) {
                if (before[0] == null) before[0] = ref.settings();
                ref.set(before[0].withOpacity(b.doubleValue() / 100), null);
                self.refresh();
            } else {
                commit.accept(b.doubleValue());
            }
        });
        slider.valueChangingProperty().addListener((o, was, now) -> {
            if (!now) commit.accept(slider.getValue());
        });
        HBox box = new HBox(8, slider, pct);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(4, 4, 4, 4));
        return box;
    }

    static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(6);
        g.setVgap(4);
        g.setPadding(new Insets(4, 4, 4, 4));
        return g;
    }

    private static void axisHeader(GridPane g, String... names) {
        for (int i = 0; i < names.length; i++) {
            Label l = new Label(names[i]);
            l.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
            g.add(l, i + 1, 0);
        }
    }

    static TextField field(double v) {
        TextField t = new TextField(num(v));
        t.setPrefColumnCount(5);
        return t;
    }

    /** Runs {@code apply} on Enter in any field, or when focus leaves the fields with something changed. */
    static void onCommit(TextField[] fields, Runnable apply) {
        String[] last = new String[fields.length];
        for (int i = 0; i < fields.length; i++) last[i] = fields[i].getText();
        Runnable maybe = () -> {
            boolean changed = false;
            for (int i = 0; i < fields.length; i++) changed |= !fields[i].getText().equals(last[i]);
            if (!changed) return;
            for (int i = 0; i < fields.length; i++) last[i] = fields[i].getText();
            apply.run();
        };
        for (TextField t : fields) {
            t.setOnAction(e -> maybe.run());
            t.focusedProperty().addListener((o, was, is) -> {
                if (!is) maybe.run();
            });
        }
    }

    /** The fields as numbers ("," as a decimal point is fine); null, with the bad fields marked, if one isn't a number. */
    static double[] parse(TextField[] fields) {
        double[] out = new double[fields.length];
        boolean ok = true;
        for (int i = 0; i < fields.length; i++) {
            try {
                out[i] = Double.parseDouble(fields[i].getText().strip().replace(',', '.'));
                fields[i].setStyle("");
            } catch (NumberFormatException e) {
                fields[i].setStyle("-fx-border-color: -color-danger-fg;");
                ok = false;
            }
        }
        return ok ? out : null;
    }

    /** A number for a field: up to three decimals, no trailing zeros. */
    static String num(double v) {
        String s = String.format(Locale.ROOT, "%.3f", v);
        s = s.contains(".") ? s.replaceAll("0+$", "").replaceAll("\\.$", "") : s;
        return s.equals("-0") ? "0" : s;
    }
}
