import SwiftUI
import shared

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}

struct QuizView: View {
    private let quizModel: QuizModel
    @StateObject private var quizModelState: ObservableState<QuizState>
    @State private var showUrlSettings = false
    @State private var baseUrlText = ""
    @State private var themeText = ""
    @FocusState private var isBaseUrlFocused: Bool
    @FocusState private var isThemeFocused: Bool

    init(quizModel: QuizModel) {
        self.quizModel = quizModel
        _quizModelState = quizModel.toStateObject { quizModel.state }
    }

    var body: some View {
        let state = quizModelState.state
        let questions = state.quiz?.questions ?? []
        let responses = state.responses

        ZStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {

                    // Header
                    HStack {
                        Text("Quiz Master 🚀")
                            .font(.largeTitle)
                            .fontWeight(.black)
                            .foregroundStyle(.tint)
                        Spacer()
                        Button {
                            withAnimation { showUrlSettings.toggle() }
                        } label: {
                            Image(systemName: showUrlSettings ? "chevron.up" : "gearshape")
                                .imageScale(.large)
                        }
                    }

                    // URL settings (collapsible)
                    if showUrlSettings {
                        TextField("Base Server URL", text: $baseUrlText)
                            .textFieldStyle(.roundedBorder)
                            .focused($isBaseUrlFocused)
                            .submitLabel(.done)
                            .onSubmit { isBaseUrlFocused = false }
                            .onChange(of: isBaseUrlFocused) { _, focused in
                                if !focused { quizModel.updateBaseUrl(baseUrl: baseUrlText) }
                            }
                    }

                    // Theme card
                    VStack(alignment: .leading, spacing: 12) {
                        TextField("Quiz Theme", text: $themeText, prompt: Text("e.g. 90s Pop Culture"))
                            .textFieldStyle(.roundedBorder)
                            .focused($isThemeFocused)
                            .submitLabel(.done)
                            .onSubmit { isThemeFocused = false }
                            .onChange(of: isThemeFocused) { _, focused in
                                if !focused { quizModel.updateQuizTheme(quizTheme: themeText) }
                            }

                        HStack(spacing: 8) {
                            Button {
                                quizModel.fetchQuiz()
                            } label: {
                                Label("Fetch", systemImage: "arrow.clockwise")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)

                            Button {
                                quizModel.resetResponses()
                            } label: {
                                Text("Reset").frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)
                        }
                    }
                    .padding(16)
                    .background(
                        RoundedRectangle(cornerRadius: 24)
                            .fill(Color(.secondarySystemBackground))
                            .shadow(color: .black.opacity(0.05), radius: 2)
                    )

                    // Error
                    if !(state.error is DomainError.NoError) {
                        Text("Oops! \(String(describing: state.error))")
                            .foregroundStyle(.red)
                            .font(.callout)
                    }

                    // Score
                    let score = zip(questions, responses).filter { question, response in
                        guard let responded = response as? Response.Responded,
                              let answer = question.answers[safe: Int(responded.answerIndex)]
                        else { return false }
                        return answer.correct
                    }.count

                    Text("Score: \(score) / \(questions.count)")
                        .font(.title2)
                        .fontWeight(.bold)

                    // Questions
                    LazyVStack(spacing: 16) {
                        ForEach(0..<questions.count, id: \.self) { qIdx in
                            QuestionCardView(
                                index: qIdx + 1,
                                question: questions[qIdx],
                                response: responses[safe: qIdx],
                                onAnswerSelected: { aIdx in
                                    quizModel.selectAnswer(
                                        questionIndex: Int32(qIdx),
                                        answerIndex: Int32(aIdx)
                                    )
                                }
                            )
                        }
                    }
                }
                .padding(16)
            }

            // Loading overlay
            if state.loading {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color(.systemBackground))
                    .frame(width: 80, height: 80)
                    .overlay { ProgressView() }
            }
        }
        .onAppear {
            baseUrlText = state.baseUrl
            themeText = state.quiz?.theme ?? ""
        }
        .onChange(of: state.baseUrl) { _, newUrl in
            if !isBaseUrlFocused { baseUrlText = newUrl }
        }
        .onChange(of: state.quiz?.theme) { _, newTheme in
            if !isThemeFocused { themeText = newTheme ?? "" }
        }
    }
}

struct QuestionCardView: View {
    let index: Int
    let question: Question
    let response: Response?
    let onAnswerSelected: (Int) -> Void

    var body: some View {
        let answers = question.answers
        let responded = response as? Response.Responded
        let hasResponded = responded != nil

        VStack(alignment: .leading, spacing: 8) {
            Text("Question \(index)")
                .font(.caption)
                .foregroundStyle(.secondary)

            Text(question.question)
                .font(.headline)
                .padding(.bottom, 4)

            ForEach(0..<answers.count, id: \.self) { aIdx in
                let answer = answers[aIdx]
                let isSelected = responded?.answerIndex == Int32(aIdx)

                Button {
                    if !hasResponded { onAnswerSelected(aIdx) }
                } label: {
                    Text(answer.answer)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .foregroundStyle(isSelected ? Color.white : Color.primary)
                }
                .buttonStyle(.borderedProminent)
                .tint(answerButtonColor(isSelected: isSelected, correct: answer.correct))
                .disabled(hasResponded && !isSelected)

                if isSelected && answer.correct, let explanation = answer.explanation {
                    Text("💡 \(explanation)")
                        .font(.caption)
                        .foregroundStyle(.tint)
                        .padding(.horizontal, 4)
                }
            }
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 28)
                .fill(Color(.systemBackground))
                .shadow(color: .black.opacity(0.05), radius: 1)
        )
    }

    private func answerButtonColor(isSelected: Bool, correct: Bool) -> Color {
        if isSelected && correct { return Color(red: 0.298, green: 0.686, blue: 0.314) }
        if isSelected { return .red }
        return Color(.secondarySystemFill)
    }
}
