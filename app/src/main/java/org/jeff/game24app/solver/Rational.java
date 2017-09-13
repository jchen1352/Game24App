package org.jeff.game24app.solver;

public class Rational implements Comparable<Rational>{

	private int numerator, denominator;
	public static final Rational CONST_24 = new Rational(24);

	public Rational(int num, int den) {
		if (den == 0) {
			throw new IllegalArgumentException("Denominator is 0");
		}
		int gcd = gcd(Math.abs(num), Math.abs(den));
		if (den < 0) {
			den = -den;
			num = -num;
		}
		numerator = num/gcd;
		denominator = den/gcd;
		if (numerator == 0) {
			denominator = 1;
		}
	}
	
	public Rational(int num) {
		this(num, 1);
	}
	
	public int getNumerator() {
		return numerator;
	}
	
	public int getDenominator() {
		return denominator;
	}
	
	public double getDoubleValue() {
		return (double) numerator/denominator;
	}
	
	public static Rational add(Rational a, Rational b) {
		int numA = a.numerator, denA = a.denominator, numB = b.numerator, denB = b.denominator;
		int lcmDen = lcm(denA, denB);
		numA *= lcmDen/denA;
		numB *= lcmDen/denB;
		return new Rational(numA+numB, lcmDen);
	}
	
	public static Rational subtract(Rational a, Rational b) {
		int numA = a.numerator, denA = a.denominator, numB = b.numerator, denB = b.denominator;
		int lcmDen = lcm(denA, denB);
		numA *= lcmDen/denA;
		numB *= lcmDen/denB;
		return new Rational(numA-numB, lcmDen);
	}
	
	public static Rational multiply(Rational a, Rational b) {
		int numA = a.numerator, denA = a.denominator, numB = b.numerator, denB = b.denominator;
		return new Rational(numA*numB, denA*denB);
	}
	
	public static Rational divide(Rational a, Rational b) {
		int numA = a.numerator, denA = a.denominator, numB = b.numerator, denB = b.denominator;
		return new Rational(numA*denB, denA*numB);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + denominator;
		result = prime * result + numerator;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rational other = (Rational) obj;
		if (denominator != other.denominator)
			return false;
		if (numerator != other.numerator)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String s = ""+numerator;
		if (denominator != 1) {
			s += "/" + denominator;
		}
		return s;
	}

	private static int gcd(int a, int b) {
		while (b > 0) {
			int temp = b;
			b = a % b;
			a = temp;
		}
		return a;
	}
	
	private static int lcm(int a, int b) {
		return a * (b / gcd(a,b));
	}

	@Override
	public int compareTo(Rational o) {
		return Double.compare(getDoubleValue(), o.getDoubleValue());
	}
}
