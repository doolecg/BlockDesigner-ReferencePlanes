package io.blockdesigner.refplanes;

import io.blockdesigner.plugin.ObjectHandle;
import io.blockdesigner.plugin.PluginContext;
import io.blockdesigner.plugin.SceneObject;
import io.blockdesigner.plugin.SceneObjectType;
import io.blockdesigner.plugin.ViewInfo;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Reference images: made from picture files (the Plugins menu, Import, or dropping a picture on the window). */
final class ReferenceType implements SceneObjectType {
    static final String ID = "reference";
    static final List<String> EXTENSIONS = List.of("png", "jpg", "jpeg", "gif", "bmp");

    private final PluginContext ctx;

    ReferenceType(PluginContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Reference image";
    }

    @Override
    public String badge() {
        return "REFERENCE";
    }

    @Override
    public SceneObject create() {
        return new ReferenceImage(ctx, this);
    }

    @Override
    public List<String> extensions() {
        return EXTENSIONS;
    }

    @Override
    public void open(Path file, ViewInfo view) throws IOException {
        add(file, view);
    }

    /** A picture read from a file and kept in the project. */
    record Loaded(String blob, String fileName, Images.Decoded decoded) {
    }

    /** Reads a picture file and stores it in the project; fails with a message for the user if it isn't one. */
    Loaded load(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        Images.Decoded d = Images.decode(bytes);
        return new Loaded(ctx.objects().storeBlob(bytes), file.getFileName().toString(), d);
    }

    /**
     * Adds a reference image of {@code file} at the view's target, facing the camera; in an orthographic axis view it
     * shows only in that view, like Blender's "align to view" references.
     */
    ObjectHandle add(Path file, ViewInfo view) throws IOException {
        Loaded l = load(file);
        ReferenceImage ref = new ReferenceImage(ctx, this);
        ref.set(ReferenceSettings.DEFAULT.withImage(l.blob(), l.fileName(), l.decoded().width(), l.decoded().height())
                .withShowIn(ReferenceSettings.showInFor(view)), l.decoded().image());
        String name = l.fileName().replaceFirst("\\.[^.]*$", "");
        ObjectHandle h = ctx.objects().add(ID, name, ReferenceSettings.placement(view), ref);
        ctx.toast("Added " + name + " · G / R move and turn it · right-click for its options");
        return h;
    }

    ViewInfo view() {
        return ctx.objects().view();
    }

    void toast(String message) {
        ctx.toast(message);
    }

    /** Plugins › Add reference image…: pick pictures and add each. */
    void chooseAndAdd() {
        List<File> files = chooser("Add reference images").showOpenMultipleDialog(owner());
        if (files == null) return;
        for (File f : files) {
            try {
                add(f.toPath(), ctx.objects().view());
            } catch (IOException e) {
                ctx.toast("✖ " + f.getName() + ": " + e.getMessage());
            }
        }
    }

    static FileChooser chooser(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pictures (PNG, JPEG, GIF, BMP)",
                EXTENSIONS.stream().map(e -> "*." + e).toList()));
        return fc;
    }

    /** The main window, to own file choosers and dialogs. */
    static Window owner() {
        Window any = null;
        for (Window w : Window.getWindows()) {
            if (!(w instanceof Stage) || !w.isShowing()) continue;
            if (w.isFocused()) return w;
            if (any == null) any = w;
        }
        return any;
    }
}
