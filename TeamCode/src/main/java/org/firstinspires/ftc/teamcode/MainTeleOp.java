package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(group="tele")
public class MainTeleOp extends OpMode {

    Drivetrain drive;
    IntakeTransferShooter intake;

    boolean isSlowMode = false;
    boolean lastAPress = false;

    @Override
    public void init() {
        drive = new Drivetrain(hardwareMap);
        intake = new IntakeTransferShooter(hardwareMap);

        telemetry.addData("Status", "Initialized");
    }

    @Override
    public void loop() {
        boolean currentAPress = gamepad1.a;
        if (currentAPress && !lastAPress) {
            isSlowMode = !isSlowMode;
        }
        lastAPress = currentAPress;

        double y = -gamepad1.left_stick_y;
        double x = gamepad1.left_stick_x * 1.1;
        double rx = gamepad1.right_stick_x;

        drive.drive(y, x, rx, isSlowMode);


        intake.update(gamepad1.left_bumper, gamepad1.left_trigger, gamepad1.right_bumper, gamepad1.dpadUpWasPressed(), gamepad1.dpadDownWasPressed());

        telemetry.addData("Shooting Power", intake.getShootingPower());
        telemetry.addData("Current RPM", intake.getActualRPM());
        telemetry.addData("Target RPM", intake.getTargetRPM());

        telemetry.update();
    }
}