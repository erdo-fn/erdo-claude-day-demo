import SwiftUI
import shared

@main
struct iOSApp: App {
    
    init() {
        OG.create()
        OG.initialize()
    }
    
	var body: some Scene {
		WindowGroup {
            QuizView(quizModel: OG[QuizModel.self])
		}
	}
}
