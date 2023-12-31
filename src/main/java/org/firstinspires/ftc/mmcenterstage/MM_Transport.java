package org.firstinspires.ftc.mmcenterstage;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class MM_Transport {
    private final LinearOpMode opMode;
    private final Telemetry dashboardTelemetry;

    private DcMotorEx slide = null;
    private DcMotorEx mtrBoxFlip = null;
    private TouchSensor bottomLimit = null;
//    private DistanceSensor boxSensor = null;

    public static int TICK_INCREMENT = 30;
//    public static double BOX_COLLECT = .14;
//    public static double BOX_SCORE = .41;
//    public static double BOX_TRANSPORT = .1256;
    public static final int UPPER_LIMIT = 2900;
    public static final int MIN_SCORE_HEIGHT = 1560;
    public static final int SLIDE_SLOW_DOWN_TICKS = 800;
    public static final int MAX_COLLECT_HEIGHT = 350;
    public static final int BOX_SCORE_TICKS = 300;
    public static final int BOX_TICK_INCREMENT = 8;
    public static final double BOX_FLIP_POWER = 0.3;
    public static final double SLIDE_HOME_POWER = -0.7;
    public static final double SLIDE_HOME_POWER_SLOW = -0.3;

    boolean readyToScore = false;
    boolean isLimitHandled = false;
    boolean isHoming = false;
    double rightStickPower = 0;  // slide
    double leftStickPower = 0;  // box flip
    int slideTargetTicks = 0;
    int boxFlipTargetTicks = 0;

    public MM_Transport(LinearOpMode opMode, Telemetry dashboardTelemetry) {
        this.opMode = opMode;
        this.dashboardTelemetry = dashboardTelemetry;
        init();
    }

    public void transport() {
        dashboardTelemetry.addData("slide pos", slide.getCurrentPosition());
        dashboardTelemetry.update();

        rightStickPower = -opMode.gamepad2.right_stick_y;
        leftStickPower = -opMode.gamepad2.left_stick_y;

        if (rightStickPower > .1) {
            isHoming = false;
        }

        if (bottomLimit.isPressed() && !isLimitHandled) {  // reset both slide & box_flip encoders
            slide.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
            slide.setPower(0);
            slide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
            slideTargetTicks = 0;
            mtrBoxFlip.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
            mtrBoxFlip.setPower(0);
            mtrBoxFlip.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
            boxFlipTargetTicks = 0;

            isLimitHandled = true;
            isHoming = false;
        } else if ((MM_TeleOp.currentGamepad2.y && !MM_TeleOp.previousGamepad2.y) && !bottomLimit.isPressed()) {
            // make sure box is at least started back down first
            mtrBoxFlip.setTargetPosition(0);
            boxFlipTargetTicks = 0;
            readyToScore = false;

            slide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
            slide.setPower(slide.getCurrentPosition() < SLIDE_SLOW_DOWN_TICKS ? SLIDE_HOME_POWER_SLOW : SLIDE_HOME_POWER);
            isHoming = true;
        } else if ((!bottomLimit.isPressed() || rightStickPower > 0.1) && !isHoming) {// not trigger or i'm trying to go up
            if (rightStickPower < -0.1) {
                slideTargetTicks = Math.max(slideTargetTicks - TICK_INCREMENT, 0);
            } else if (rightStickPower > 0.1) {
                slideTargetTicks = Math.min(slideTargetTicks + TICK_INCREMENT, UPPER_LIMIT);
            }

            slide.setTargetPosition(slideTargetTicks);
            slide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
            slide.setPower(1);
            if (!bottomLimit.isPressed()) {
                isLimitHandled = false;
            }
        }

//        if (slide.getCurrentPosition() > MIN_SCORE_HEIGHT) {
//            if (!MM_TeleOp.previousGamepad2.right_stick_button && MM_TeleOp.currentGamepad2.right_stick_button) {
//                readyToScore = !readyToScore;
//                boxFlip.setPosition((readyToScore) ? BOX_SCORE : BOX_TRANSPORT);
//                mtrBoxFlip.setTargetPosition((readyToScore) ? MTR_BOX_SCORE : 0);
//            }
//        } else {
//            boxFlip.setPosition((slide.getCurrentPosition() > MAX_COLLECT_HEIGHT) ? BOX_TRANSPORT : BOX_COLLECT);
        //mtrBoxFlip.setTargetPosition(0);
        // }
//        if (slide.getCurrentPosition() > MAX_COLLECT_HEIGHT) {

        if (slide.getCurrentPosition() > MIN_SCORE_HEIGHT && !MM_TeleOp.previousGamepad2.right_stick_button && MM_TeleOp.currentGamepad2.right_stick_button) {
            readyToScore = !readyToScore;
            boxFlipTargetTicks = (readyToScore) ? BOX_SCORE_TICKS : 0;
        }
        if (!bottomLimit.isPressed()) {
            if (leftStickPower > 0.1) {
                boxFlipTargetTicks += BOX_TICK_INCREMENT;
            } else if (leftStickPower < -0.1) {
                boxFlipTargetTicks -= BOX_TICK_INCREMENT;
            }

            mtrBoxFlip.setTargetPosition(boxFlipTargetTicks);
            mtrBoxFlip.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
            mtrBoxFlip.setPower(BOX_FLIP_POWER);
        } else {
            mtrBoxFlip.setPower(0);
        }
//        else if (opMode.gamepad2.left_stick_button) {//reset mtr box flip 0
//            mtrBoxFlip.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
//            mtrBoxFlip.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
//            boxFlipTargetTicks = 0;
//            mtrBoxFlip.setPower(1);
//        }

//        mtrBoxFlip.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        dashboardTelemetry.addData("Box pos", mtrBoxFlip.getCurrentPosition());
    }

    public void runToScorePos(){
        mtrBoxFlip.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        mtrBoxFlip.setTargetPosition(0);
        mtrBoxFlip.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        mtrBoxFlip.setPower(BOX_FLIP_POWER);
        slide.setTargetPosition(MIN_SCORE_HEIGHT);
        slide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        slide.setPower(.5);
        while (slide.isBusy()){
            opMode.telemetry.addData("Slide running", slide.getCurrentPosition());
            opMode.telemetry.update();
        }
        mtrBoxFlip.setTargetPosition(BOX_SCORE_TICKS);

        while(mtrBoxFlip.isBusy()){
            opMode.telemetry.addData("Box Flipping", mtrBoxFlip.getCurrentPosition());
            opMode.telemetry.update();
        }
    }

    public void goHome(){
        mtrBoxFlip.setTargetPosition(0);
        while (mtrBoxFlip.isBusy()){ }
        slide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        slide.setPower(slide.getCurrentPosition() < SLIDE_SLOW_DOWN_TICKS ? SLIDE_HOME_POWER_SLOW : SLIDE_HOME_POWER);
        while(!bottomLimit.isPressed()){ }
        slide.setPower(0);
    }

    public void init() {
        slide = opMode.hardwareMap.get(DcMotorEx.class, "slide");

        bottomLimit = opMode.hardwareMap.get(TouchSensor.class, "bottomLimit");
        //boxSensor = opMode.hardwareMap.get(DistanceSensor.class, "boxSensor");

        slide.setDirection(DcMotorEx.Direction.REVERSE);

        slide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        slide.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        mtrBoxFlip = opMode.hardwareMap.get(DcMotorEx.class, "mtrBoxFlip");
        mtrBoxFlip.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        mtrBoxFlip.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
//        mtrBoxFlip.setTargetPosition(0);
//        mtrBoxFlip.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        mtrBoxFlip.setPower(0);
    }
}
