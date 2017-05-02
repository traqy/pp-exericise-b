package com.paypal.exercises;

import java.lang.Math;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.lang.IllegalArgumentException;
import java.util.ArrayList;

public class AmortizationSchedule {
		
	private ArrayList<AmortizationPayment> paymentSchedules  = new ArrayList<AmortizationPayment>();
	
	private long amountBorrowed = 0;		// in cents
	private double annualPercentageRate = 0d;
	private int initialTermMonths = 0;
	
	private final static double NUMBER_OF_MONTHS_A_YEAR = 12d;
	private final static double MONTHLY_INTEREST_DIVISOR = NUMBER_OF_MONTHS_A_YEAR * 100d;
	private double monthlyInterest = 0d;
	private long monthlyPaymentAmount = 0;	// in cents

	private static final double[] BORROW_AMOUNT_RANGE = new double[] { 0.01d, 1000000000000d };
	private static final double[] ANNUAL_PERCENTAGE_RATE = new double[] { 0.000001d, 100d };
	private static final int[] TERM_RANGE = new int[] { 1, 1000000 };
	
	private static final String INVALID_BORROW_AMOUNT_MSG = "Invalid argument value for borrow amount.";
	private static final String INVALID_INTEREST_RATE_MSG = "Invalid argument value for interest rate.";
	private static final String INVALID_TERM_YEARS = "Invalid argument value for term years";
	
	// The output should include:
	//	The first column identifies the payment number.
	//	The second column contains the amount of the payment.
	//	The third column shows the amount paid to interest.
	//	The fourth column has the current balance.  The total payment amount and the interest paid fields.
	
	public AmortizationSchedule(double amount, double interestRate, int years) throws IllegalArgumentException {

		if ( !isValidBorrowAmount(amount) ) {
			throw new IllegalArgumentException(INVALID_BORROW_AMOUNT_MSG);
		}
		if ( !isValidAPRValue(interestRate) ) {
			throw new IllegalArgumentException(INVALID_INTEREST_RATE_MSG);
		}
		if (!isValidTerm(years)) {
			throw new IllegalArgumentException(INVALID_TERM_YEARS);			
		}
		

		amountBorrowed = Math.round(amount * 100);
		annualPercentageRate = interestRate;
		initialTermMonths = years * 12;
		
		monthlyPaymentAmount = calculateMonthlyPayment();
				
		// the following shouldn't happen with the available valid ranges
		// for borrow amount, apr, and term; however, without range validation,
		// monthlyPaymentAmount as calculated by calculateMonthlyPayment()
		// may yield incorrect values with extreme input values
		if (monthlyPaymentAmount > amountBorrowed) {
			throw new IllegalArgumentException();
		}
		else {
			calculateAmortizationSchedule();
		}

	}
	
	private long calculateMonthlyPayment() {
		// M = P * (J / (1 - (Math.pow(1/(1 + J), N))));
		//
		// Where:
		// P = Principal
		// I = Interest
		// J = Monthly Interest in decimal form:  I / (12 * 100)
		// N = Number of months of loan
		// M = Monthly Payment Amount
		// 
		
		// calculate J
		monthlyInterest = annualPercentageRate / MONTHLY_INTEREST_DIVISOR;
		
		// this is 1 / (1 + J)
		double tmp = Math.pow(1d + monthlyInterest, -1);
		
		// this is Math.pow(1/(1 + J), N)
		tmp = Math.pow(tmp, initialTermMonths);
		
		// this is 1 / (1 - (Math.pow(1/(1 + J), N))))
		tmp = Math.pow(1d - tmp, -1);
		
		// M = P * (J / (1 - (Math.pow(1/(1 + J), N))));
		double rc = amountBorrowed * monthlyInterest * tmp;
		
		return Math.round(rc);
	}

	
	private void calculateAmortizationSchedule() {
		// 
		// To create the amortization table, create a loop in your program and follow these steps:
		// 1.      Calculate H = P x J, this is your current monthly interest
		// 2.      Calculate C = M - H, this is your monthly payment minus your monthly interest, so it is the amount of principal you pay for that month
		// 3.      Calculate Q = P - C, this is the new balance of your principal of your loan.
		// 4.      Set P equal to Q and go back to Step 1: You thusly loop around until the value Q (and hence P) goes to zero.
		// 

		
		long balance = amountBorrowed;
		int paymentNumber = 0;
		long totalPayments = 0;
		long totalInterestPaid = 0;

		AmortizationPayment borrow = new AmortizationPayment(paymentNumber, 0, 0, amountBorrowed, totalPayments, totalInterestPaid);
		paymentSchedules.add(borrow);
		
		final int maxNumberOfPayments = initialTermMonths + 1;
		while ((balance > 0) && (paymentNumber <= maxNumberOfPayments)) {
			// Calculate H = P x J, this is your current monthly interest
			long curMonthlyInterest = Math.round(((double) balance) * monthlyInterest);

			// the amount required to payoff the loan
			long curPayoffAmount = balance + curMonthlyInterest;
			
			// the amount to payoff the remaining balance may be less than the calculated monthlyPaymentAmount
			long curMonthlyPaymentAmount = Math.min(monthlyPaymentAmount, curPayoffAmount);
			
			// it's possible that the calculated monthlyPaymentAmount is 0,
			// or the monthly payment only covers the interest payment - i.e. no principal
			// so the last payment needs to payoff the loan
			if ((paymentNumber == maxNumberOfPayments) &&
					((curMonthlyPaymentAmount == 0) || (curMonthlyPaymentAmount == curMonthlyInterest))) {
				curMonthlyPaymentAmount = curPayoffAmount;
			}
			
			// Calculate C = M - H, this is your monthly payment minus your monthly interest,
			// so it is the amount of principal you pay for that month
			long curMonthlyPrincipalPaid = curMonthlyPaymentAmount - curMonthlyInterest;
			
			// Calculate Q = P - C, this is the new balance of your principal of your loan.
			long curBalance = balance - curMonthlyPrincipalPaid;
			
			totalPayments += curMonthlyPaymentAmount;
			totalInterestPaid += curMonthlyInterest;
			
			// Store the payment information data to ArrayList of AmortizationPayment objects
			AmortizationPayment payment = new AmortizationPayment(++paymentNumber, curMonthlyPaymentAmount, curMonthlyInterest, curBalance, totalPayments, totalInterestPaid);
			
			paymentSchedules.add(payment);
			
			balance = curBalance;
		}
	}
	
	public ArrayList<AmortizationPayment> getAmortizationPaymentSchedule() {
		return this.paymentSchedules;
	}
	
	public long getMonthlyPayment() {
		return this.monthlyPaymentAmount;
	}
	
	public void consoleOutput(){
		
		String formatString = "%1$-20s%2$-20s%3$-20s%4$s,%5$s,%6$s\n";
		Utils.printf(formatString,
				"PaymentNumber", "PaymentAmount", "PaymentInterest",
				"CurrentBalance", "TotalPayments", "TotalInterestPaid");

		for (AmortizationPayment ap: paymentSchedules ){
			int paymentNumber = ap.getPaymentNumber();
			long paymentAmount = ap.getMonthlyPaymentAmount();
			long paymentInterest = ap.getMonthlyInterest();
			long currentBalance = ap.getBalance();
			long totalPayments = ap.getTotalPayment();
			long totalInterestPaid = ap.getTotalInterestPaid();
		
			// output is in dollars
			Utils.printf(formatString, paymentNumber,
					((double) paymentAmount) / 100d,
					((double) paymentInterest) / 100d,
					((double) currentBalance) / 100d,
					((double) totalPayments) / 100d,
					((double) totalInterestPaid) / 100d);
		}
	}
	
	public static boolean isValidBorrowAmount(double amount) {
		double range[] = getBorrowAmountRange();
		return ((range[0] <= amount) && (amount <= range[1]));
	}
	
	public static boolean isValidAPRValue(double rate) {
		double range[] = getAPRRange();
		return ((range[0] <= rate) && (rate <= range[1]));
	}
	
	public static boolean isValidTerm(int years) {
		int range[] = getTermRange();
		return ((range[0] <= years) && (years <= range[1]));
	}
	
	public static final double[] getBorrowAmountRange() {
		return BORROW_AMOUNT_RANGE;
	}
	
	public static final double[] getAPRRange() {
		return ANNUAL_PERCENTAGE_RATE;
	}

	public static final int[] getTermRange() {
		return TERM_RANGE;
	}
	
	class AmortizationPayment {
		
		private int paymentNumber;
		private long monthlyPaymentAmount;
		private long monthlyInterest;
		private long balance;
		private long totalPayment;
		private long totalInterestPaid;
		
		// Store the payment information data to ArrayList of HashMap
		//    Payment Number, Monthly Payment Amount, Monthly Interest Balance, Total Payments, TotalInterestPaid

		AmortizationPayment( int paymentNumber, long monthlyPaymentAmount, long monthlyInterest, long balance, long totalPayments, long totalInterestPaid) {
			try {
				// TODO validation
				this.paymentNumber = paymentNumber;
				this.monthlyPaymentAmount = monthlyPaymentAmount;
				this.monthlyInterest = monthlyInterest;
				this.balance = balance;
				this.totalPayment = totalPayments;
				this.totalInterestPaid = totalInterestPaid;
				
			}
			catch (Exception e){
			}
		}
		
		public int getPaymentNumber() {
			return paymentNumber;
		}
		public long getMonthlyPaymentAmount() {
			return this.monthlyPaymentAmount;
		}
		public long getMonthlyInterest() {
			return this.monthlyInterest;
		}
		public long getBalance() {
			return this.balance;
		}
		public long getTotalPayment() {
			return this.totalPayment;
		}
		public long getTotalInterestPaid() {
			return this.totalInterestPaid;
		}
	}

	public static void main(String [] args) {
		String[] userPrompts = {
				"Please enter the amount you would like to borrow: ",
				"Please enter the annual percentage rate used to repay the loan: ",
				"Please enter the term, in years, over which the loan is repaid: "
		};
		
		String line = "";
		double amount = 0;
		double apr = 0;
		int years = 0;
		
		for (int i = 0; i< userPrompts.length; ) {
			String userPrompt = userPrompts[i];
			try {
				line = Utils.readLine(userPrompt);
			} catch (IOException e) {
				Utils.print("An IOException was encountered. Terminating program.\n");
				return;
			}
			
			boolean isValidValue = true;
			try {
				switch (i) {
				case 0:
					amount = Double.parseDouble(line);
					if (isValidBorrowAmount(amount) == false) {
						isValidValue = false;
						double range[] = getBorrowAmountRange();
						Utils.print("Please enter a positive value between " + range[0] + " and " + range[1] + ". ");
					}
					break;
				case 1:
					apr = Double.parseDouble(line);
					if (isValidAPRValue(apr) == false) {
						isValidValue = false;
						double range[] = getAPRRange();
						Utils.print("Please enter a positive value between " + range[0] + " and " + range[1] + ". ");
					}
					break;
				case 2:
					years = Integer.parseInt(line);
					if (isValidTerm(years) == false) {
						isValidValue = false;
						int range[] = getTermRange();
						Utils.print("Please enter a positive integer value between " + range[0] + " and " + range[1] + ". ");
					}
					break;
				}
			} catch (NumberFormatException e) {
				isValidValue = false;
			}
			if (isValidValue) {
				i++;
			} else {
				Utils.print("An invalid value was entered.\n");
			}
		}
		try {
			AmortizationSchedule as = new AmortizationSchedule(amount, apr, years);
			as.consoleOutput();;
		} catch (IllegalArgumentException e) {
			Utils.print("Unable to process the values entered.");
			Utils.print(e.getMessage());
			Utils.print("Terminating program.\n");
		}
	}
}

