//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorEx;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//
//public class Drivetrain {
//    private DcMotorEx frontLeft, frontRight, backLeft, backRight;
//
//    public Drivetrain(HardwareMap hardwareMap) {
//        frontLeft = hardwareMap.get(DcMotorEx.class, "leftFront");
//        frontRight = hardwareMap.get(DcMotorEx.class, "rightFront");
//        backLeft = hardwareMap.get(DcMotorEx.class, "leftBack");
//        backRight = hardwareMap.get(DcMotorEx.class, "rightBack");
//
//        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
//        backRight.setDirection(DcMotorSimple.Direction.REVERSE);
//
//        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//    }
//
//    public void drive(double y, double x, double rx, boolean slowMode) {
//        double multiplier = slowMode ? 0.2 : 1.0;
//
//        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
//        double frontLeftPower = (y - x - rx) / denominator;
//        double backLeftPower = (y + x - rx) / denominator;
//        double frontRightPower = (y + x + rx) / denominator;
//        double backRightPower = (y - x + rx) / denominator;
//
//        frontLeft.setPower(frontLeftPower * multiplier);
//        backLeft.setPower(backLeftPower * multiplier);
//        frontRight.setPower(frontRightPower * multiplier);
//        backRight.setPower(backRightPower * multiplier);
//    }
//}

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Drivetrain {
    private DcMotorEx frontLeft, frontRight, backLeft, backRight;

    public Drivetrain(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotorEx.class, "leftFront");
        frontRight = hardwareMap.get(DcMotorEx.class, "rightFront");
        backLeft = hardwareMap.get(DcMotorEx.class, "leftBack");
        backRight = hardwareMap.get(DcMotorEx.class, "rightBack");

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void drive(double y, double x, double rx, boolean slowMode) {
        double multiplier = slowMode ? 0.2 : 1.0;

        y = -y;

        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

        double frontRightPower = (y + x + rx) / denominator;
        double frontLeftPower  = (y - x - rx) / denominator;
        double backRightPower  = (y - x + rx) / denominator;
        double backLeftPower   = (y + x - rx) / denominator;

        frontLeft.setPower(frontLeftPower * multiplier);
        backLeft.setPower(backLeftPower * multiplier);
        frontRight.setPower(frontRightPower * multiplier);
        backRight.setPower(backRightPower * multiplier);
    }
}