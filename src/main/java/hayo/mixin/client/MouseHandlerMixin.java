package hayo.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hayo.common.SkipMixinsAtReturnException;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @WrapOperation(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
    private boolean invokeMouseClickedEvents(Screen screen, MouseButtonEvent ctx, boolean doubleClick, Operation<Boolean> operation) {
        try {
            return operation.call(screen, ctx, doubleClick);
        } catch (SkipMixinsAtReturnException e) {
            return true;
        }
    }
}
