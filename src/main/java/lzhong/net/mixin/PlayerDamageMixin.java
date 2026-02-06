package lzhong.net.mixin;



import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class PlayerDamageMixin {


    @Inject(method = "init", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        System.out.println("TestMixin applied!");
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onApplyDamage(DamageSource source, float amount, CallbackInfo ci) {
        // Log to console if the mixin is being invoked
        System.out.println("Damage method called! Damage source: " + source + ", Amount: " + amount);

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        // Track the first time this method is called

        // Current health before damage is applied
        float currentHealth = player.getHealth();

        // Predict if the player will die from this damage
        if (amount >= currentHealth) {
            // Call the logic to handle "predicted death"
            handleFatalDamage(player, source, amount);
        }
    }

    private void handleFatalDamage(ClientPlayerEntity player, DamageSource source, float amount) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Notify the player
        player.sendMessage(Text.literal("You're about to die! Damage: " + amount + " > Health: " + player.getHealth()), false);

        boolean hasTotem = false;

        // Check if the player has a Totem of Undying
        for (ItemStack item : player.getInventory().main) {
            if (item.getItem() == Items.TOTEM_OF_UNDYING) {
                hasTotem = true;
                break;
            }
        }

        if (!hasTotem) {
            // Disconnect the player as per the logic
            client.getNetworkHandler().getConnection().disconnect(Text.literal("You would have died!"));

            // Show the disconnect screen
            client.setScreen(new DisconnectedScreen(
                    client.currentScreen,
                    Text.literal("Disconnected"),
                    Text.literal("You would have died!")
            ));
        }
    }
}