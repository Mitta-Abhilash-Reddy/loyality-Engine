class ChurnScorer:
    """
    Rule-based churn scoring engine.
    Returns a score between 0.0 (no risk) and 1.0 (certain churn).
    """

    RISK_THRESHOLDS = {
        "HIGH":   0.7,
        "MEDIUM": 0.4,
        "LOW":    0.0,
    }

    def score(
        self,
        customer_id: int,
        days_since_last_transaction: int,
        total_points: int,
        tier: str,
        active_last_7_days: bool = False,
    ) -> dict:
        raw_score = 0.0
        factors = []

        # ── Recency rules ──────────────────────────────────────────────
        if days_since_last_transaction > 30:
            raw_score += 0.4
            factors.append(f"Inactive {days_since_last_transaction}d (>30d, +0.40)")
        elif days_since_last_transaction > 14:
            raw_score += 0.2
            factors.append(f"Inactive {days_since_last_transaction}d (>14d, +0.20)")

        # ── Points rules ───────────────────────────────────────────────
        if total_points == 0:
            raw_score += 0.3
            factors.append("Zero total points (+0.30)")

        # ── Tier rules ─────────────────────────────────────────────────
        tier_upper = tier.upper() if tier else "BRONZE"
        if tier_upper == "BRONZE":
            raw_score += 0.1
            factors.append("BRONZE tier (+0.10)")

        # ── Engagement boost ───────────────────────────────────────────
        if active_last_7_days:
            raw_score -= 0.2
            factors.append("Active last 7 days (−0.20)")

        # Clamp to [0.0, 1.0]
        churn_score = round(max(0.0, min(1.0, raw_score)), 4)

        risk_level = "LOW"
        if churn_score >= self.RISK_THRESHOLDS["HIGH"]:
            risk_level = "HIGH"
        elif churn_score >= self.RISK_THRESHOLDS["MEDIUM"]:
            risk_level = "MEDIUM"

        return {
            "customerId": customer_id,
            "churnScore": churn_score,
            "riskLevel": risk_level,
            "factors": factors,
        }
