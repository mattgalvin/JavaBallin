package net.liquidchaos.controller;

public class XBoxControllerData {
	private float leftStickX;
	private float leftStickY;
	private float rightStickX;
	private float rightStickY;
	
	private int leftAngle;
	private float leftRadius;

	private int rightAngle;
	private float rightRadius;

	public XBoxControllerData(float leftX, float leftY, float rightX, float rightY) {
		leftStickX = leftX;
		leftStickY = leftY;
		rightStickX = rightX;
		rightStickY = rightY;
		
		double leftAngleD = Math.toDegrees(Math.atan2(leftX, leftY));
		leftAngle = (int)(leftAngleD < 0.0f ? 360.0f + leftAngleD : leftAngleD);
		leftRadius = (float)Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));

		double rightAngleD = Math.toDegrees(Math.atan2(rightX, rightY));
		rightAngle = (int)(rightAngleD < 0.0f ? 360.0f + rightAngleD : rightAngleD);
		rightRadius = (float)Math.sqrt(Math.pow(rightX, 2) + Math.pow(rightY, 2));
	}

	public float getLeftStickX() {
		return leftStickX;
	}

	public float getLeftStickY() {
		return leftStickY;
	}

	public float getRightStickX() {
		return rightStickX;
	}

	public float getRightStickY() {
		return rightStickY;
	}

	public int getLeftAngle() {
		return leftAngle;
	}

	public float getLeftRadius() {
		return leftRadius;
	}

	public int getRightAngle() {
		return rightAngle;
	}

	public float getRightRadius() {
		return rightRadius;
	}
}
