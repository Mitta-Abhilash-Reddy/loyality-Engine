from flask import Flask, request, jsonify
from scorer import ChurnScorer
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
scorer = ChurnScorer()


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "UP", "service": "churn-scoring-service"}), 200


@app.route("/score", methods=["POST"])
def score():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body is required"}), 400

    required_fields = ["customerId", "daysSinceLastTransaction", "totalPoints", "tier"]
    missing = [f for f in required_fields if f not in data]
    if missing:
        return jsonify({"error": f"Missing required fields: {missing}"}), 400

    try:
        result = scorer.score(
            customer_id=data["customerId"],
            days_since_last_transaction=data["daysSinceLastTransaction"],
            total_points=data["totalPoints"],
            tier=data["tier"],
            active_last_7_days=data.get("activeLastSevenDays", False),
        )
        logger.info(f"Scored customer {data['customerId']}: {result['churnScore']:.2f} ({result['riskLevel']})")
        return jsonify(result), 200

    except Exception as e:
        logger.error(f"Scoring error for customer {data.get('customerId')}: {e}")
        return jsonify({"error": "Internal scoring error", "detail": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8001, debug=False)
