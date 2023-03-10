package de.dhbw.rahmlab.geomalgelang.api;

import de.orat.math.cga.api.CGAAttitudeVectorIPNS;
import de.orat.math.cga.api.CGAAttitudeVectorOPNS;
import de.orat.math.cga.api.CGACircleOPNS;
import de.orat.math.cga.api.CGALineOPNS;
import de.orat.math.cga.api.CGAMultivector;
import de.orat.math.cga.api.CGAPlaneIPNS;
import de.orat.math.cga.api.CGAPointPairOPNS;
import de.orat.math.cga.api.CGARoundPointIPNS;
import de.orat.math.cga.api.CGARoundPointOPNS;
import de.orat.math.cga.api.CGAScalarIPNS;
import de.orat.math.cga.api.CGAScalarOPNS;
import de.orat.math.cga.api.CGASphereIPNS;
import de.orat.math.cga.api.CGATangentVectorOPNS;
import de.orat.math.cga.api.CGATranslator;
import java.util.HashMap;
import java.util.Map;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Tuple3d;
import org.jogamp.vecmath.Vector3d;

public class Arguments {

	protected final Map<String, CGAMultivector> argsMap = new HashMap<>();

	protected void put(String argName, CGAMultivector multivector) throws IllegalArgumentException {
		if (this.argsMap.containsKey(argName)) {
			throw new IllegalArgumentException("argName \"" + argName + "\" is already present.");
		}

		this.argsMap.put(argName, multivector);
	}

	public Arguments scalar(String argName, double scalar) {
		var mvec = new CGAScalarOPNS(scalar);
		this.put(argName, mvec);
		return this;
	}

	public Arguments point_ipns(String argName, Tuple3d point) {
		var mvec = new CGARoundPointIPNS(point);
		this.put(argName, mvec);
		return this;
	}

	public Arguments point_opns(String argName, Point3d point) {
		var mvec = new CGARoundPointOPNS(point);
		this.put(argName, mvec);
		return this;
	}

	public Arguments pointpair_opns(String argName, Point3d point1, double weight1, Point3d point2, double weight2) {
		var mvec = new CGAPointPairOPNS(point1, weight1, point2, weight2);
		this.put(argName, mvec);
		return this;
	}

	public Arguments pointpair_opns(String argName, Tuple3d point1, Tuple3d point2) {
		var mvec = new CGAPointPairOPNS(new CGARoundPointIPNS(point1), new CGARoundPointIPNS(point2));
		this.put(argName, mvec);
		return this;
	}

	public Arguments line_opns(String argName, Point3d point1, double weight1, Point3d point2, double weight2) {
		var mvec = new CGALineOPNS(point1, weight1, point2, weight2);
		this.put(argName, mvec);
		return this;
	}

	public Arguments line_opns(String argName, Point3d point1, Point3d point2) {
		var mvec = new CGALineOPNS(point1, point2);
		this.put(argName, mvec);
		return this;
	}

	public Arguments sphere_ipns(String argName, Point3d center, double radius, double weight) {
		var mvec = new CGASphereIPNS(center, radius, weight);
		this.put(argName, mvec);
		return this;
	}

	public Arguments sphere_ipns(String argName, Point3d center, double radius) {
		var mvec = new CGASphereIPNS(center, radius);
		this.put(argName, mvec);
		return this;
	}

	public Arguments plane_ipns(String argName, Vector3d normal, double dist, double weight) {
		var mvec = new CGAPlaneIPNS(normal, dist, weight);
		this.put(argName, mvec);
		return this;
	}

	public Arguments plane_ipns(String argName, Vector3d normal, double dist) {
		return this.plane_ipns(argName, normal, dist, 1.0);
	}

	public Arguments circle_opns(String argName, Point3d point1, double weight1, Point3d point2, double weight2, Point3d point3, double weight3) {
		var mvec = new CGACircleOPNS(point1, weight1, point2, weight2, point3, weight3);
		this.put(argName, mvec);
		return this;
	}

	public Arguments circle_opns(String argName, Point3d point1, Point3d point2, Point3d point3) {
		var mvec = new CGACircleOPNS(point1, point2, point3);
		this.put(argName, mvec);
		return this;
	}

	public Arguments tangent_opns(String argName, Point3d location, Vector3d direction) {
		var mvec = new CGATangentVectorOPNS(location, direction);
		this.put(argName, mvec);
		return this;
	}

	public Arguments attitude_ipns(String argName, Vector3d t) {
		var mvec = new CGAAttitudeVectorIPNS(t);
		this.put(argName, mvec);
		return this;
	}

	public Arguments attitude_opns(String argName, Vector3d t) {
		var mvec = new CGAAttitudeVectorOPNS(t);
		this.put(argName, mvec);
		return this;
	}

	public Arguments translator(String argName, Vector3d point) {
		var mvec = new CGATranslator(point);
		this.put(argName, mvec);
		return this;
	}
}
