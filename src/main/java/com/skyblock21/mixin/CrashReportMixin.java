package com.skyblock21.mixin;

import com.skyblock21.util.dev.CrashReportCollector;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CrashReport.class, priority = 900)
public class CrashReportMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(String message, Throwable cause, CallbackInfo ci) {
        CrashReportCollector.handleCrash((CrashReport) (Object) this);
    }

    @Inject(method = "asString(Lnet/minecraft/util/crash/ReportType;)Ljava/lang/String;", at = @At("TAIL"), cancellable = true)
    private void onAsString(ReportType type, CallbackInfoReturnable<String> cir) {
        if (!CrashReportCollector.isSkyblock21Crash((CrashReport) (Object) this)) return;

        StringBuilder sb = new StringBuilder(cir.getReturnValue());
        sb.append("\n\n============================================================\n");
        sb.append("\tSkyblock21 may have caused this crash.\n\n");
        sb.append("\tPlease join the discord for support\n\n");
        sb.append("\t> https://discord.gg/NMNSwQH6dr\n");
        sb.append("============================================================\n");
        cir.setReturnValue(sb.toString());
    }

}
