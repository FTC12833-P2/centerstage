package org.firstinspires.ftc.mmcenterstage;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

import java.util.List;

public class MM_Drivetrain {
    private final LinearOpMode opMode;
    private final ElapsedTime timer = new ElapsedTime();

    @Config
    public static class DashboardConstants {
        public static double MAX_DRIVE_POWER = .7;
        public static double APRIL_TAG_THRESHOLD = 2;
        public static double DRIVE_P_COEFF = .0166;
        public static double MIN_DRIVE_POWER = .14;
        public static double MAX_DETECT_ATTEMPTS = 150;
    }

    private DcMotorEx flMotor = null;
    private DcMotorEx frMotor = null;
    private DcMotorEx blMotor = null;
    private DcMotorEx brMotor = null;

    public MM_AprilTags aprilTags;

    private final Gamepad currentGamepad1;
    private final Gamepad previousGamepad1;
    private final Telemetry dashboardTelemetry;
    boolean isSlow = false;

    int detectAttemptCount = 0;

    public MM_Drivetrain(LinearOpMode opMode, Gamepad currentGamepad1, Gamepad previousGamepad1, Telemetry dashboardTelemetry) {
        this.opMode = opMode;
        this.currentGamepad1 = currentGamepad1;
        this.previousGamepad1 = previousGamepad1;
        this.dashboardTelemetry = dashboardTelemetry;
        init();
    }

    public void driveWithSticks() {
        double drivePower = -opMode.gamepad1.left_stick_y;
        double strafePower = opMode.gamepad1.left_stick_x;
        double rotatePower = opMode.gamepad1.right_stick_x;
        opMode.telemetry.addData("test", "push");

        double FLPower = drivePower + strafePower + rotatePower;
        double FRPower = drivePower - strafePower - rotatePower;
        double BLPower = drivePower - strafePower + rotatePower;
        double BRPower = drivePower + strafePower - rotatePower;

        if (currentGamepad1.a && !previousGamepad1.a) {
            isSlow = !isSlow;
        }

        double maxMotorPwr = Math.max(Math.max(Math.abs(FLPower), Math.abs(FRPower)),
                Math.max(Math.abs(BLPower), Math.abs(BRPower)));

        if (maxMotorPwr > 1) {
            FLPower /= maxMotorPwr;
            FRPower /= maxMotorPwr;
            BLPower /= maxMotorPwr;
            BRPower /= maxMotorPwr;
        }

        if (isSlow) {
            FLPower *= 0.5;
            FRPower *= 0.5;
            BLPower *= 0.5;
            BRPower *= 0.5;
        }

        flMotor.setPower(FLPower * 0.7);
        frMotor.setPower(FRPower * 0.7);
        blMotor.setPower(BLPower * 0.7);
        brMotor.setPower(BRPower * 0.7);

        previousGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(opMode.gamepad1);
    }

    public void driveToAprilTag() {
        double errorY = 0;
        double errorX = 0;
        boolean keepGoing = true;
        detectAttemptCount = 0;

        timer.reset();
        while (opMode.opModeIsActive() && keepGoing) {
            AprilTagDetection tagId = getId(2);

            if(tagId != null){
                errorY = getErrorY(6, tagId);
                errorX = getErrorX(0, tagId);
                detectAttemptCount = 0;

                double power = errorY * DashboardConstants.DRIVE_P_COEFF * DashboardConstants.MAX_DRIVE_POWER;
                power = Math.min(power, DashboardConstants.MAX_DRIVE_POWER);
                power = Math.max(power, DashboardConstants.MIN_DRIVE_POWER);

                flMotor.setPower(power);
                frMotor.setPower(power);
                blMotor.setPower(power);
                brMotor.setPower(power);

                if(Math.abs(errorY) <= DashboardConstants.APRIL_TAG_THRESHOLD && Math.abs(errorX) >= DashboardConstants.APRIL_TAG_THRESHOLD) {
                    keepGoing = false;
                }
            } else {
                detectAttemptCount++;
                if(detectAttemptCount >= DashboardConstants.MAX_DETECT_ATTEMPTS){
                    keepGoing = false;
                }
                opMode.sleep(1);
            }

            dashboardTelemetry.addData("errorY", errorY);
            dashboardTelemetry.addData("detect attempts", detectAttemptCount);
//            dashboardTelemetry.addData("power", power);
            dashboardTelemetry.update();
        }//end while keep going

        flMotor.setPower(0);
        frMotor.setPower(0);
        blMotor.setPower(0);
        brMotor.setPower(0);
    }

    private double getErrorY(double target, AprilTagDetection tagId) {
            return  tagId.ftcPose.y - target;
        }

    private double getErrorX(double target, AprilTagDetection tagId) {
        return target - tagId.ftcPose.x;
    }

    public AprilTagDetection getId(int id) {
        List<AprilTagDetection> currentDetections = aprilTags.aprilTagProcessor.getDetections();

        for (AprilTagDetection detection : currentDetections) {
            if (detection.id == id) {
                return detection;
            }
        }
        return null;
    }

    public void init() {
        flMotor = opMode.hardwareMap.get(DcMotorEx.class, "flMotor");
        frMotor = opMode.hardwareMap.get(DcMotorEx.class, "frMotor");
        blMotor = opMode.hardwareMap.get(DcMotorEx.class, "blMotor");
        brMotor = opMode.hardwareMap.get(DcMotorEx.class, "brMotor");

        flMotor.setDirection(DcMotorEx.Direction.REVERSE);
        blMotor.setDirection(DcMotorEx.Direction.REVERSE);

        aprilTags = new MM_AprilTags(opMode);

        dashboardTelemetry.addData("detect attempts",  detectAttemptCount);
        dashboardTelemetry.update();
    }
}

