package padenormous.giddyup209.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.RenderTickCounter;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

public class FlightHUD {
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static Vec3d lastVelocity = Vec3d.ZERO;
    private static long lastTime = System.nanoTime();
    private static double maxGForce = 0.0;
    private static double maxAcceleration = 0.0;
    private static double maxSpeed = 0.0;
    private static Vec3d startPosition = null;
    private static final int SMOOTH_SAMPLES = 5;
    private static Queue<Double> recentSpeeds = new LinkedList<>();
    private static final int HUD_COLOR = 0xFF66CCFF; // Light blue color
    private static final int BACKGROUND_COLOR = 0x80000000; // Semi-transparent black (alpha: 0x80)
    private static final int PADDING = 2; // Padding around text

    public static void register() {
        HudRenderCallback.EVENT.register((DrawContext context, RenderTickCounter tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;

            if (player != null && player.isFallFlying()) {
                if (startPosition == null) {
                    startPosition = player.getPos();
                }

                TextRenderer textRenderer = client.textRenderer;
                int x = 10;
                int y = 240;
                int lineHeight = textRenderer.fontHeight + 2;

                // Calculate metrics as before...
                Vec3d velocity = player.getVelocity();
                double instantSpeed = velocity.length() * 20;

                recentSpeeds.offer(instantSpeed);
                if (recentSpeeds.size() > SMOOTH_SAMPLES) {
                    recentSpeeds.poll();
                }
                double currentSpeed = recentSpeeds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                if (currentSpeed > maxSpeed) {
                    maxSpeed = currentSpeed;
                }

                long currentTime = System.nanoTime();
                double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
                Vec3d accelerationVector = velocity.subtract(lastVelocity).multiply(1.0 / deltaTime);
                double accelerationMagnitude = accelerationVector.length() * 20;

                if (accelerationMagnitude > maxAcceleration) {
                    maxAcceleration = accelerationMagnitude;
                    maxGForce = accelerationMagnitude / 9.81;
                }

                Vec3d currentPos = player.getPos();
                double distanceX = Math.abs(currentPos.x - startPosition.x);
                double distanceY = Math.abs(currentPos.y - startPosition.y);
                double distanceZ = Math.abs(currentPos.z - startPosition.z);
                double totalDistance = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);

                String direction = getDirection(player.getYaw());
                lastVelocity = velocity;
                lastTime = currentTime;

                // Prepare text lines
                String[] lines = {
                        "Direction: " + direction,
                        String.format("Current Speed: %.1f m/s", currentSpeed),
                        String.format("Max Speed: %.1f m/s", maxSpeed),
                        String.format("Max Acceleration: %.1f m/s²", maxAcceleration),
                        String.format("Max G-Force: %.1f G", maxGForce),
                        String.format("Position: X: %.1f, Y: %.1f, Z: %.1f", currentPos.x, currentPos.y, currentPos.z),
                        String.format("Distance Traveled: %.1f blocks", totalDistance)
                };

                // Find the widest line for background width
                int maxWidth = 0;
                for (String line : lines) {
                    maxWidth = Math.max(maxWidth, textRenderer.getWidth(line));
                }

                // Draw background
                int backgroundHeight = (lineHeight * lines.length);
                context.fill(x - PADDING,
                        y - PADDING,
                        x + maxWidth + PADDING,
                        y + backgroundHeight + PADDING,
                        BACKGROUND_COLOR);

                // Draw text lines
                for (int i = 0; i < lines.length; i++) {
                    context.drawText(textRenderer,
                            Text.literal(lines[i]),
                            x,
                            y + (i * lineHeight),
                            HUD_COLOR,
                            true);
                }

            } else {
                maxGForce = 0.0;
                maxAcceleration = 0.0;
                maxSpeed = 0.0;
                startPosition = null;
                recentSpeeds.clear();
            }
        });
    }

    private static String getDirection(float yaw) {
        yaw = (yaw % 360);
        if (yaw < 0) yaw += 360;
        if (yaw >= 337.5 || yaw < 22.5) return "South";
        if (yaw < 67.5) return "Southwest";
        if (yaw < 112.5) return "West";
        if (yaw < 157.5) return "Northwest";
        if (yaw < 202.5) return "North";
        if (yaw < 247.5) return "Northeast";
        if (yaw < 292.5) return "East";
        if (yaw < 337.5) return "Southeast";
        return "South";
    }
}