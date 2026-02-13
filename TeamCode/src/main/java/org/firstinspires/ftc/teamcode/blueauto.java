package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Config
@Autonomous(group = "Autonomous")
public class blueauto extends LinearOpMode {

    public class IntakeTransferShooter {
        private DcMotorEx intakeBottom, intakeTop, flywheelTop, flywheelBottom;
        private Servo intakeLatch;

        public static final double LATCH_OPEN = 0.7;
        public static final double LATCH_CLOSED = 0.2;
        public static final double TARGET_RPM = 1200;
        public static final double TICKS_PER_REV = 28;

        public IntakeTransferShooter(HardwareMap hardwareMap) {
            intakeBottom = hardwareMap.get(DcMotorEx.class, "intakeBottom");
            intakeTop = hardwareMap.get(DcMotorEx.class, "intakeTop");
            intakeLatch = hardwareMap.get(Servo.class, "transferGate");
            flywheelTop = hardwareMap.get(DcMotorEx.class, "outtakeTop");
            flywheelBottom = hardwareMap.get(DcMotorEx.class, "outtakeBottom");

            flywheelTop.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            flywheelBottom.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            intakeLatch.setPosition(LATCH_CLOSED);
        }

        public class StartIntake implements Action {
            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                intakeBottom.setPower(1.0);
                intakeTop.setPower(-1.0);
                return false;
            }
        }
        public Action startIntake() { return new StartIntake(); }

        public class StopIntake implements Action {
            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                intakeBottom.setPower(0);
                intakeTop.setPower(0);
                return false;
            }
        }
        public Action stopIntake() { return new StopIntake(); }

        public class ShootAction implements Action {
            private final ElapsedTime timer = new ElapsedTime();
            private boolean gateOpened = false;
            private boolean intakeStarted = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                double currentRPM = (flywheelTop.getVelocity() * 60.0) / TICKS_PER_REV;
                packet.put("Flywheel RPM", currentRPM);

                flywheelTop.setVelocity((TARGET_RPM * TICKS_PER_REV) / 60.0);
                flywheelBottom.setVelocity((TARGET_RPM * TICKS_PER_REV) / 60.0);

                if (currentRPM < (TARGET_RPM - 100) && !gateOpened) {
                    return true;
                }

                if (!gateOpened) {
                    intakeLatch.setPosition(LATCH_OPEN);
                    timer.reset();
                    gateOpened = true;
                    return true;
                }

                if (gateOpened && !intakeStarted) {
                    if (timer.seconds() < 1.0) {
                        return true;
                    } else {
                        intakeBottom.setPower(1.0);
                        intakeTop.setPower(-1.0);
                        timer.reset();
                        intakeStarted = true;
                        return true;
                    }
                }

                if (intakeStarted) {
                    if (timer.seconds() < 2.0) {
                        return true;
                    } else {
                        // Sequence Complete: Cleanup
                        intakeBottom.setPower(0);
                        intakeTop.setPower(0);
                        intakeLatch.setPosition(LATCH_CLOSED);
                        return false;
                    }
                }

                return true;
            }
        }

        public Action shoot() { return new ShootAction(); }
    }

    @Override
    public void runOpMode() {
        Pose2d initialPose = new Pose2d(-48.2, -49.2, Math.toRadians(44.93));
        MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);
        IntakeTransferShooter shooter = new IntakeTransferShooter(hardwareMap);

        Action path1 = drive.actionBuilder(initialPose)
                .lineToX(-23)
                .build();
//        Action path2 = drive.actionBuilder(new Pose2d(-23, -49.2, Math.toRadians(44.93)))
//                .strafeTo(new Vector2d(-62, -50)
//                .build();
        waitForStart();
        if (isStopRequested()) return;

        Actions.runBlocking(
                new SequentialAction(
                        path1,
                        shooter.shoot(),
//                        path2,
                        shooter.stopIntake(),
                        shooter.shoot()
                )
        );
    }
}