import AVKit
import Shared

final class TransportBarObserverImpl: NSObject, TransportBarObserver, AVPlayerViewControllerDelegate {

    func observe(controller: AVPlayerViewController) {
        controller.delegate = self
    }

    func playerViewController(
        _ playerViewController: AVPlayerViewController,
        willTransitionToVisibilityOfTransportBar visible: Bool,
        with coordinator: AVPlayerViewControllerAnimationCoordinator
    ) {
        TransportBarObserverHolder.shared.notifyVisibilityChanged(visible: visible)
    }
}
