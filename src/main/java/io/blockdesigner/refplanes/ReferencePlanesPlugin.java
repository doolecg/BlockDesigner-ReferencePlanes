package io.blockdesigner.refplanes;

import io.blockdesigner.plugin.BlockDesignerPlugin;
import io.blockdesigner.plugin.PluginAction;
import io.blockdesigner.plugin.PluginContext;

/**
 * Reference Planes: pictures placed in the scene to build from, like Blender's reference images. They are listed in
 * the Layers panel as REFERENCE, moved and turned with the Move and Rotate tools, and set up from their right-click
 * menu (Properties, opacity, the views they show in, whether blocks cover them, flips…). How new pictures start out
 * is set on its page in BlockDesigner's Settings window.
 */
public final class ReferencePlanesPlugin implements BlockDesignerPlugin {
    @Override
    public void enable(PluginContext ctx) {
        ReferenceType type = new ReferenceType(ctx);
        ctx.registerObjectType(type);
        ctx.registerSettings(NewPictures.OPTIONS, v -> type.setNewPictures(NewPictures.of(v)));
        ctx.registerAction(new PluginAction("Add reference image…",
                "A picture to build from, facing the view (or drop a picture on the window)", type::chooseAndAdd));
    }
}
