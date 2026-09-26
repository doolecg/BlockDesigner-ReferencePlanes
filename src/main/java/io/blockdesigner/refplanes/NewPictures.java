package io.blockdesigner.refplanes;

import io.blockdesigner.plugin.Drawing;
import io.blockdesigner.plugin.OptionValues;
import io.blockdesigner.plugin.Options;

import java.util.List;

/**
 * How new pictures start out, from the settings in the plugin's tab: their height, opacity, whether blocks cover them,
 * and whether one added in an axis view shows only in that view. Each picture can still be changed afterwards.
 */
record NewPictures(double height, double opacity, Drawing.Depth depth, boolean onlyInItsView) {
    static final List<String> DRAW = List.of("Behind blocks", "In the scene", "In front of blocks");

    static final Options OPTIONS = Options.builder()
            .decimal("height", "Height (blocks)", ReferenceSettings.DEFAULT_SIZE, 1, 512)
            .decimal("opacity", "Opacity", 1, 0.05, 1)
            .choice("draw", "Draw", DRAW, DRAW.get(1))
            .toggle("onlyInItsView", "Added in an axis view: show only there", true)
            .build();

    static final NewPictures DEFAULT = of(OPTIONS.defaults());

    static NewPictures of(OptionValues v) {
        Drawing.Depth depth = switch (DRAW.indexOf(v.choice("draw"))) {
            case 0 -> Drawing.Depth.BEHIND_BLOCKS;
            case 2 -> Drawing.Depth.IN_FRONT;
            default -> Drawing.Depth.IN_SCENE;
        };
        return new NewPictures(v.decimal("height"), v.decimal("opacity"), depth, v.toggle("onlyInItsView"));
    }
}
