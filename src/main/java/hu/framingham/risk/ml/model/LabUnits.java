package hu.framingham.risk.ml.model;

/** Converts lab values from the API unit (mmol/L) to the Framingham dataset unit (mg/dL). */
public final class LabUnits {
    /** Total cholesterol: 1 mmol/L = 38.67 mg/dL. */
    public static final double CHOLESTEROL_MG_DL_PER_MMOL_L = 38.67;
    /** Glucose: 1 mmol/L = 18.016 mg/dL (molar mass 180.16 g/mol). */
    public static final double GLUCOSE_MG_DL_PER_MMOL_L = 18.016;

    private LabUnits() {}

    public static Double cholesterolToMgDl(Double mmolPerLiter) {
        return mmolPerLiter == null ? null : mmolPerLiter * CHOLESTEROL_MG_DL_PER_MMOL_L;
    }

    public static Double glucoseToMgDl(Double mmolPerLiter) {
        return mmolPerLiter == null ? null : mmolPerLiter * GLUCOSE_MG_DL_PER_MMOL_L;
    }
}
