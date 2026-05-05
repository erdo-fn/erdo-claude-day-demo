import Foundation
import shared

/*
 get dependencies from anywhere like this counterModel = OG[CounterModel.self]
 */
final class OG {

    private static let instance = OG()
    private var dependencies: [String: Any] = [:]
    
    private var initialized = false

    private init() {}

    static func create() {
        IosDIKt.doInitKoinIos()
        instance.register(IosDIKt.getQuizModel(), as: QuizModel.self)
    }
    
    static func initialize() {
        if (!instance.initialized) {
            instance.initialized = true

            // run any necessary initialization code once object graph has been created here

        }
    }

    private func register<T>(_ dependency: T, as type: T.Type) {
        dependencies[String(describing: type)] = dependency
    }
    
    static subscript<T>(type: T.Type) -> T {
        guard let dependency = instance.dependencies[String(describing: type)] as? T else {
            fatalError("No dependency registered for type \(type)")
        }
        return dependency
    }

    func putMock<T>(_ instance: T, as type: T.Type) {
        OG.instance.register(instance, as: type)
    }
}
