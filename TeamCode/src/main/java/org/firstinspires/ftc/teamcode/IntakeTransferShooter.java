package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Config
public class IntakeTransferShooter {
    private static DcMotorEx intakeBottom;
    private DcMotorEx intakeTop;
    private DcMotorEx flywheelTop;
    private DcMotorEx flywheelBottom;
    private Servo intakeLatch;

    public static double LATCH_CLOSED = 0.2;
    public static double LATCH_OPEN = 0.7;

    private ElapsedTime shootTimer = new ElapsedTime();
    private boolean isShooting = false;
    private double shootingPower = 0.7;
    private double targetRPM = 1450;
    private final double TICKS_PER_REV = 28;

    public static double readyToleranceRPM = 150;
    public static double dipThresholdRPM = 250;
    public static double recoveryBoostRPM = 700;
    public static double recoveryTime = 0.6;

    public static double overshootThresholdRPM = 100;
    public static double brakeBoostRPM = 1000;

    private ElapsedTime gateTimer = new ElapsedTime();
    private boolean gateWasClosed = true;
    private double gateOpenDelay = 0.35;

    private ElapsedTime recoveryTimer = new ElapsedTime();
    private boolean isRecovering = false;

    private ElapsedTime intakeRampTimer = new ElapsedTime();
    private double baseIntakePower = 0.7;

    public IntakeTransferShooter(HardwareMap hardwareMap) {
        intakeBottom = hardwareMap.get(DcMotorEx.class, "intakeBottom");
        intakeTop = hardwareMap.get(DcMotorEx.class, "intakeTop");
        intakeLatch = hardwareMap.servo.get("transferGate");
        flywheelTop = hardwareMap.get(DcMotorEx.class, "outtakeTop");
        flywheelBottom = hardwareMap.get(DcMotorEx.class, "outtakeBottom");
        intakeLatch.setPosition(LATCH_CLOSED);

        flywheelTop.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheelBottom.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

//    public void update(boolean Intakebtn, double trigger, boolean shootBtn, boolean up, boolean down) {
//        if (up) {
//            targetRPM += 50.0;
//        } else if (down) {
//            targetRPM -= 50.0;
//        }
//        targetRPM = Math.max(0, targetRPM);
//
//        if (shootBtn && !isShooting) {
//            shoot();
//        }
//
//        double velocityTarget;
//        if (isShooting) {
//            velocityTarget = rpmToTicksPerSec(targetRPM);
//            if (shootTimer.seconds() < 1.5) {
//                intakeLatch.setPosition(LATCH_OPEN);
//                intakeBottom.setPower(1.0);
//                intakeTop.setPower(-1.0);
//            } else {
//                isShooting = false;
//                intakeLatch.setPosition(LATCH_CLOSED);
//            }
//        } else {
//            velocityTarget = rpmToTicksPerSec(1200.0);
//            if (Intakebtn) {
//                intakeBottom.setPower(1);
//                intakeTop.setPower(-1);
//                intakeLatch.setPosition(LATCH_CLOSED);
//            } else if (trigger > 0.1) {
//                intakeBottom.setPower(-trigger);
//                intakeTop.setPower(trigger);
//                intakeLatch.setPosition(LATCH_CLOSED);
//            } else {
//                intakeBottom.setPower(0);
//                intakeTop.setPower(0);
//                intakeLatch.setPosition(LATCH_CLOSED);
//            }
//        }



//        flywheelTop.setVelocity(velocityTarget);
//        flywheelBottom.setVelocity(velocityTarget);
//    }

    public void update(boolean Intakebtn, double trigger, boolean shootBtn, boolean up, boolean down) {
        if (up) {
            targetRPM += 50.0;
        } else if (down) {
            targetRPM -= 50.0;
        }
        targetRPM = Math.max(0, targetRPM);

        if (!shootBtn) {
            intakeRampTimer.reset();
        }

        isShooting = shootBtn;

        double actualRPM = getActualRPM();
        double currentEffectiveTarget = targetRPM;

        if (actualRPM < (targetRPM - dipThresholdRPM) && !isRecovering) {
            isRecovering = true;
            recoveryTimer.reset();
        }

        if (isRecovering) {
            if (recoveryTimer.seconds() < recoveryTime) {
                currentEffectiveTarget = targetRPM + recoveryBoostRPM;
            } else {
                isRecovering = false;
            }
        }

        if (actualRPM > (targetRPM + overshootThresholdRPM)) {
            currentEffectiveTarget = targetRPM - brakeBoostRPM;
        }

        double velocityTarget = rpmToTicksPerSec(currentEffectiveTarget);
        flywheelTop.setVelocity(velocityTarget);
        flywheelBottom.setVelocity(velocityTarget);

        if (isShooting) {
            boolean readyToFire = Math.abs(actualRPM - targetRPM) < readyToleranceRPM;

            if (readyToFire) {
                intakeLatch.setPosition(LATCH_OPEN);

                if (gateWasClosed) {
                    gateTimer.reset();
                    gateWasClosed = false;
                }

                if (gateTimer.seconds() > gateOpenDelay) {
                    double currentRampPower = baseIntakePower + (Math.floor(intakeRampTimer.seconds()) * 0.1);
                    currentRampPower = Math.min(currentRampPower, 1.0); // Cap at 1.0

                    intakeBottom.setPower(currentRampPower);
                    intakeTop.setPower(-currentRampPower);
                } else {
                    intakeBottom.setPower(0);
                    intakeTop.setPower(0);
                }
            } else {
                intakeLatch.setPosition(LATCH_CLOSED);
                intakeBottom.setPower(0);
                intakeTop.setPower(0);
                gateWasClosed = true;
            }
        } else {
            intakeLatch.setPosition(LATCH_CLOSED);
            gateWasClosed = true;

            if (Intakebtn) {
                intakeBottom.setPower(1);
                intakeTop.setPower(-1);
            } else if (trigger > 0.1) {
                intakeBottom.setPower(-trigger);
                intakeTop.setPower(trigger);
            } else {
                intakeBottom.setPower(0);
                intakeTop.setPower(0);
            }
        }
    }


    public double getShootingPower() {
        return shootingPower;
    }

    public double getTargetRPM() {
        return targetRPM;
    }

    public double getActualRPM() {
        return (flywheelTop.getVelocity() * 60.0) / TICKS_PER_REV;
    }

    public void shoot() {
        isShooting = true;
        shootTimer.reset();
    }

    private double rpmToTicksPerSec(double rpm) {
        return (rpm * TICKS_PER_REV) / 60.0;
    }
}

