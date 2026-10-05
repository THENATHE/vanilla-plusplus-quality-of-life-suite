package me.pajic.toolpouch.hud;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.ToolPouchClient;
import me.pajic.toolpouch.compat.SeasonsCompat;
import me.pajic.toolpouch.util.CompatFlags;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class InfoOverlays {
    private static final List<ObjectIntImmutablePair<Component>> renderList = new ArrayList<>();
    private static final Minecraft MC = Minecraft.getInstance();
	private static final int WHITE = 0xffffffff;

    public static void render(GuiGraphicsExtractor guiGraphics) {
        if (!CompatFlags.IMMERSIVE_OVERLAYS_LOADED && MC.player != null && MC.level != null && !MC.gui.hud.isHidden() && !MC.gui.hud.getDebugOverlay().showDebugScreen()) {
            boolean shouldObfuscateCompass = ToolPouch.CONFIG.infoOverlaySettings.obfuscateCompassIfNotOverworld.get() && MC.level.dimension() != Level.OVERWORLD;
            boolean shouldObfuscateClock = ToolPouch.CONFIG.infoOverlaySettings.obfuscateClockIfNotOverworld.get() && MC.level.dimension() != Level.OVERWORLD;
            if (ToolPouchUtil.toolPouchHasItem(MC.player, stack -> stack.is(Items.COMPASS))) {
                prepareCompassOverlay(shouldObfuscateCompass);
            }
			if (ToolPouchUtil.toolPouchHasItem(MC.player, stack -> stack.is(Items.CLOCK)))  {
                prepareClockOverlay(shouldObfuscateClock);
            }
			if (CompatFlags.SEASONS_LOADED && SeasonsCompat.toolPouchHasCalendar(MC.player)) {
				prepareSeasonOverlay(shouldObfuscateClock);
			}
            if (ToolPouchUtil.toolPouchHasItem(MC.player, stack -> stack.is(Items.RECOVERY_COMPASS))) {
                prepareRecoveryCompassOverlay();
            }
            if (!renderList.isEmpty()) {
                renderLines(guiGraphics);
                renderList.clear();
            }
        }
    }

    @SuppressWarnings("resource")
	private static void prepareCompassOverlay(boolean shouldObfuscate) {
        if (shouldObfuscate) {
            if (ToolPouch.CONFIG.infoOverlaySettings.useObfuscationEffect.get()) {
                Component obfuscatedText = makeObfuscatedString();
				boolean coordinates = ToolPouch.CONFIG.infoOverlaySettings.overlayFields.coordinates.get();
				boolean direction = ToolPouch.CONFIG.infoOverlaySettings.overlayFields.direction.get();
				if (ToolPouchClient.CONFIG.infoOverlaySettings.combinedPositionAndDirection.get()) {
					if (coordinates || direction) renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
				} else {
					if (coordinates) renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
					if (direction) renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
				}
                if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.biome.get())
                    renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
            }
        } else {
            BlockPos blockPos = MC.player.blockPosition();

			if (ToolPouchClient.CONFIG.infoOverlaySettings.combinedPositionAndDirection.get()) {
				MutableComponent combined = Component.empty();

				if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.direction.get()) {
					combined.append(Component.literal(MC.player.getDirection().getName().substring(0, 1).toUpperCase(Locale.ROOT)));
				}

				if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.coordinates.get()) {
					Component coordinates;
					if (!combined.equals(Component.empty())) combined.append(" ");
					if (ToolPouch.CONFIG.infoOverlaySettings.showYCoordinate.get()) {
						coordinates = Component.translatable(
								"gui.toolpouch.coordinates_xyz",
								blockPos.getX(), blockPos.getY(), blockPos.getZ()
						);
					} else {
						coordinates = Component.translatable(
								"gui.toolpouch.coordinates_xz",
								blockPos.getX(), blockPos.getZ()
						);
					}
					combined.append(coordinates);
				}

				renderList.add(new ObjectIntImmutablePair<>(combined, WHITE));
	        } else {
				if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.coordinates.get()) {
					Component coordinates;
					if (ToolPouch.CONFIG.infoOverlaySettings.showYCoordinate.get()) {
						coordinates = Component.translatable(
								"gui.toolpouch.coordinates_xyz",
								blockPos.getX(), blockPos.getY(), blockPos.getZ()
						);
					} else {
						coordinates = Component.translatable(
								"gui.toolpouch.coordinates_xz",
								blockPos.getX(), blockPos.getZ()
						);
					}
					renderList.add(new ObjectIntImmutablePair<>(coordinates, WHITE));
				}

				if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.direction.get()) {
					Component direction = Component.translatable("gui.toolpouch.facing", MC.player.getDirection().getName());
					renderList.add(new ObjectIntImmutablePair<>(direction, WHITE));
				}
			}

            if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.biome.get()) {
				MC.player.level().getBiome(blockPos).unwrapKey().ifPresentOrElse(key -> {
					Identifier id = key.identifier();
					renderList.add(new ObjectIntImmutablePair<>(
							Component.translatable("biome." + id.getNamespace() + "." + id.getPath()), WHITE
					));
				}, () -> renderList.add(new ObjectIntImmutablePair<>(
						Component.translatable("gui.toolpouch.biome_unknown"), WHITE
				)));
            }
        }
    }

    private static void prepareClockOverlay(boolean shouldObfuscate) {
        if (shouldObfuscate) {
            if (ToolPouch.CONFIG.infoOverlaySettings.useObfuscationEffect.get()) {
                Component obfuscatedText = makeObfuscatedString();
                if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.dayAndTime.get())
                    renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
                if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.weather.get())
                    renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
                if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.moonPhase.get())
                    renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
            }
        } else {
            BlockPos blockPos = MC.player.blockPosition();

            if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.dayAndTime.get()) {
                MutableComponent dayAndTime = Component.translatable(
                        "gui.toolpouch.day",
                        (MC.level.getDefaultClockTime() / 24000L) + 1
                );
                long timeOffset = (MC.level.getDefaultClockTime() + 6000) % 24000;
                Component time = Component.translatable(
                        "gui.toolpouch.time",
                        timeOffset / 1000,
                        String.format("%02d", (int) ((double) (timeOffset / 10 % 100) / 100 * 60))
                );
                dayAndTime.append(", ");
                dayAndTime.append(time);
                renderList.add(new ObjectIntImmutablePair<>(dayAndTime, WHITE));
            }

            if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.weather.get()) {
                Component weather;
                int weatherColor;
                if (MC.level.isThundering()) {
                    weather = Component.translatable("gui.toolpouch.thundering");
                    weatherColor = ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.thundering.get().argb();
                } else if (MC.level.isRaining()) {
                    Biome.Precipitation precipitation = MC.level.getBiome(blockPos).value().getPrecipitationAt(blockPos, (int) MC.player.getY());
                    if (precipitation == Biome.Precipitation.RAIN) {
                        weather = Component.translatable("gui.toolpouch.raining");
                        weatherColor = ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.raining.get().argb();
                    } else if (precipitation == Biome.Precipitation.SNOW) {
                        weather = Component.translatable("gui.toolpouch.snowing");
                        weatherColor = ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.snowing.get().argb();
                    } else {
                        weather = Component.translatable("gui.toolpouch.cloudy");
                        weatherColor = ToolPouchClient.CONFIG.infoOverlaySettings.overlayColors.cloudy.get().argb();
                    }
                } else {
                    weather = Component.translatable("gui.toolpouch.clear");
                    weatherColor = WHITE;
                }
                if (ToolPouchClient.CONFIG.infoOverlaySettings.coloredWeather.get()) {
                    renderList.add(new ObjectIntImmutablePair<>(weather, weatherColor));
                } else {
                    renderList.add(new ObjectIntImmutablePair<>(weather, WHITE));
                }
            }

            if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.moonPhase.get()) {
                /* Decided to flip the emojis around due to how they are displayed in-game;
                full moon is a hollow circle, new moon is a filled circle
                It doesn't feel right so I shifted them to match the MC moon more -Meep*/
                MutableComponent moonPhase;
                switch (MC.gameRenderer.gameRenderState().levelRenderState.skyRenderState.moonPhase.index()) {
                    case 0 -> moonPhase = Component.literal("\uD83C\uDF11 ").append(
                            Component.translatable("gui.toolpouch.full_moon"));
                    case 1 -> moonPhase = Component.literal("\uD83C\uDF18 ").append(
                            Component.translatable("gui.toolpouch.waning_gibbous"));
                    case 2 -> moonPhase = Component.literal("\uD83C\uDF17 ").append(
                            Component.translatable("gui.toolpouch.last_quarter"));
                    case 3 -> moonPhase = Component.literal("\uD83C\uDF16 ").append(
                            Component.translatable("gui.toolpouch.waning_crescent"));
                    case 4 -> moonPhase = Component.literal("\uD83C\uDF15 ").append(
                            Component.translatable("gui.toolpouch.new_moon"));
                    case 5 -> moonPhase = Component.literal("\uD83C\uDF14 ").append(
                            Component.translatable("gui.toolpouch.waxing_crescent"));
                    case 6 -> moonPhase = Component.literal("\uD83C\uDF13 ").append(
                            Component.translatable("gui.toolpouch.first_quarter"));
                    case 7 -> moonPhase = Component.literal("\uD83C\uDF12 ").append(
                            Component.translatable("gui.toolpouch.waxing_gibbous"));
                    default -> moonPhase = Component.literal("\uD83D\uDCA5 ").append(
                            Component.translatable("gui.toolpouch.moon_default"));
                }
                renderList.add(new ObjectIntImmutablePair<>(moonPhase, WHITE));
            }
        }
    }

	private static void prepareSeasonOverlay(boolean shouldObfuscate) {
		if (shouldObfuscate) {
			if (ToolPouch.CONFIG.infoOverlaySettings.useObfuscationEffect.get()) {
				Component obfuscatedText = makeObfuscatedString();
				if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.season.get())
					renderList.add(new ObjectIntImmutablePair<>(obfuscatedText, WHITE));
			}
		} else {
			if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.season.get()) {
				if (CompatFlags.SEASONS_LOADED) {
					ObjectIntImmutablePair<Component> seasonStringData = SeasonsCompat.getSeasonStringData(MC.level);
					if (ToolPouchClient.CONFIG.infoOverlaySettings.coloredSeason.get()) {
						renderList.add(seasonStringData);
					} else {
						renderList.add(new ObjectIntImmutablePair<>(seasonStringData.left(), WHITE));
					}
				}
			}
		}
	}

    private static void prepareRecoveryCompassOverlay() {
        if (ToolPouch.CONFIG.infoOverlaySettings.overlayFields.lastDeathLocation.get()) {
            Optional<GlobalPos> optional = MC.player.getLastDeathLocation();
            if (optional.isPresent()) {
                if (optional.get().dimension() == MC.level.dimension()) {
                    BlockPos lastDeathLocation = optional.get().pos();
                    Component coordinates;
                    if (ToolPouch.CONFIG.infoOverlaySettings.showYCoordinate.get()) {
                        coordinates = Component.translatable(
                                "gui.toolpouch.coordinates_xyz",
                                lastDeathLocation.getX(), lastDeathLocation.getY(), lastDeathLocation.getZ()
                        );
                    } else {
                        coordinates = Component.translatable(
                                "gui.toolpouch.coordinates_xz",
                                lastDeathLocation.getX(), lastDeathLocation.getZ()
                        );
                    }
                    renderList.add(new ObjectIntImmutablePair<>(
                            Component.translatable("gui.toolpouch.last_death_location"), WHITE
                    ));
                    renderList.add(new ObjectIntImmutablePair<>(coordinates, WHITE));
                } else {
                    renderList.add(new ObjectIntImmutablePair<>(
                            Component.translatable("gui.toolpouch.last_death_location_wrong_dimension"), WHITE
                    ));
                }
            } else {
                renderList.add(new ObjectIntImmutablePair<>(
                        Component.translatable("gui.toolpouch.last_death_location_unavailable"), WHITE
                ));
            }
        }
    }

    private static void renderLines(GuiGraphicsExtractor guiGraphics) {
		int scaleOffset = Math.round(128 * ToolPouchClient.CONFIG.minimapOverlaySettings.size.get());
		int offset = switch (ToolPouchClient.CONFIG.minimapOverlaySettings.minimapBackgroundStyle.get()) {
			case TEXTURE -> 12;
			case CLEAR -> 6;
			case NONE -> 4;
		} + ToolPouchClient.CONFIG.minimapOverlaySettings.offsetY.get() + scaleOffset;
        int y = MinimapOverlay.minimapActive &&
				ToolPouchClient.CONFIG.minimapOverlaySettings.position.get() == ToolPouchClient.CONFIG.infoOverlaySettings.position.get() ?
				6 + offset : 4;
        OverlayPosition position = ToolPouchClient.CONFIG.infoOverlaySettings.position.get();
        if (position == OverlayPosition.BOTTOM_LEFT || position == OverlayPosition.BOTTOM_RIGHT) {
            Collections.reverse(renderList);
        }
        for (ObjectIntImmutablePair<Component> line : renderList) {
            renderLine(guiGraphics, MC.font, line.left(), y, line.rightInt());
            y += 12;
        }
    }

    private static void renderLine(GuiGraphicsExtractor guiGraphics, Font font, Component text, int lineY, int color) {
        int width = MC.getWindow().getGuiScaledWidth();
        int height = MC.getWindow().getGuiScaledHeight();
        int offsetX = ToolPouchClient.CONFIG.infoOverlaySettings.offsetX.get();
        int offsetY = ToolPouchClient.CONFIG.infoOverlaySettings.offsetY.get();
        int raisedOffsetX = 0;
        int raisedOffsetY = 0;
        if (CompatFlags.RAISED_LOADED) {
            IntIntImmutablePair offsets = RaisedCompat.getOtherComponentOffsets();
            raisedOffsetX = offsets.leftInt();
            raisedOffsetY = offsets.rightInt();
        }

        IntIntImmutablePair position;
        switch (ToolPouchClient.CONFIG.infoOverlaySettings.position.get()) {
            case TOP_RIGHT -> position = new IntIntImmutablePair(
                    width - 4 - MC.font.width(text) - offsetX + raisedOffsetX,
                    lineY + offsetY + raisedOffsetY
            );
            case BOTTOM_LEFT -> position = new IntIntImmutablePair(
                    4 + offsetX + raisedOffsetX,
                    height - 8 - lineY - offsetY + raisedOffsetY
            );
            case BOTTOM_RIGHT -> position = new IntIntImmutablePair(
                    width - 4 - MC.font.width(text) - offsetX + raisedOffsetX,
                    height - 8 - lineY - offsetY + raisedOffsetY
            );
            default -> position = new IntIntImmutablePair(
                    4 + offsetX + raisedOffsetX,
                    lineY + offsetY + raisedOffsetY
            );
        }
        int x = position.leftInt();
        int y = position.rightInt();

        if (ToolPouchClient.CONFIG.infoOverlaySettings.textBackground.get()) {
            guiGraphics.fill(
                    x - 2, y - 2, x + font.width(text) + 2, y + 10,
					ARGB.color(
							ARGB.as8BitChannel(ToolPouchClient.CONFIG.infoOverlaySettings.textBackgroundOpacity.get()),
                            0, 0, 0
                    )
            );
        }
        guiGraphics.text(font, text, x, y, color, ToolPouchClient.CONFIG.infoOverlaySettings.textShadow.get());
    }

	private static Component makeObfuscatedString() {
		return Component.literal(
				"" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED +
				"XXXXXXXX".substring(0, MC.level.getRandom().nextInt(4) + 3)
		);
	}
}
