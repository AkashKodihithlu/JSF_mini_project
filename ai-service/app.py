from flask import Flask, request, jsonify
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)

# Starter TF-IDF Duplicate Checker
@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "AI/ML service running"})

@app.route("/check-duplicate", methods=["POST"])
def check_duplicate():
    data = request.get_json() or {}
    new_desc = data.get("description", "")
    existing_descriptions = data.get("existing", [])

    if not new_desc or not existing_descriptions:
        return jsonify({"is_duplicate": False, "max_similarity": 0.0})

    corpus = existing_descriptions + [new_desc]
    vectorizer = TfidfVectorizer(stop_words="english")
    tfidf_matrix = vectorizer.fit_transform(corpus)
    similarities = cosine_similarity(tfidf_matrix[-1], tfidf_matrix[:-1]).flatten()
    max_sim = float(similarities.max())

    return jsonify({
        "is_duplicate": max_sim >= 0.75,
        "max_similarity": round(max_sim, 4)
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
