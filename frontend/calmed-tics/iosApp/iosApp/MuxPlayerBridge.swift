import AVFoundation
import AVKit
@preconcurrency import MuxPlayerSwift
@preconcurrency import Shared
import UIKit

@MainActor
final class MuxPlayerBridge: NSObject, @preconcurrency IosVideoPlayerBridge {

    func createController(
        items: [VideoItem],
        startIndex: Int32,
        quality: VideoQuality,
        muted: Bool,
        listener: IosVideoPlayerListener
    ) -> UIViewController {
        let controller = MuxPlayerViewController()
        controller.load(
            items: items,
            startIndex: Int(startIndex),
            quality: quality,
            muted: muted,
            listener: listener
        )
        return controller
    }

    func update(
        controller: UIViewController,
        items: [VideoItem],
        startIndex: Int32,
        quality: VideoQuality,
        muted: Bool
    ) {
        (controller as? MuxPlayerViewController)?.update(
            items: items,
            startIndex: Int(startIndex),
            quality: quality,
            muted: muted
        )
    }

    func releaseController(controller: UIViewController) {
        (controller as? MuxPlayerViewController)?.teardown()
    }
}

final class MuxPlayerViewController: UIViewController {

    private let playerViewController = AVPlayerViewController()
    private var items: [VideoItem] = []
    private var qualityName: String = "R720"
    private var index: Int = 0
    private weak var listener: IosVideoPlayerListener?
    private var endObserver: NSObjectProtocol?
    private var timeControlObserver: NSKeyValueObservation?

    private lazy var previousButton: UIButton = makeOverlayButton(
        systemName: "backward.end.fill",
        action: #selector(goToPrevious)
    )
    private lazy var nextButton: UIButton = makeOverlayButton(
        systemName: "forward.end.fill",
        action: #selector(goToNext)
    )

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .black
        playerViewController.view.backgroundColor = .black
        addChild(playerViewController)
        playerViewController.view.frame = view.bounds
        playerViewController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(playerViewController.view)
        playerViewController.didMove(toParent: self)

        setupOverlayControls()
        setupTapRecognizer()
    }

    func load(
        items: [VideoItem],
        startIndex: Int,
        quality: VideoQuality,
        muted: Bool,
        listener: IosVideoPlayerListener
    ) {
        self.items = items
        self.qualityName = quality.name
        self.index = clamp(startIndex)
        self.listener = listener
        configureCurrentItem(muted: muted, resumeTime: nil)
    }

    func update(
        items: [VideoItem],
        startIndex: Int,
        quality: VideoQuality,
        muted: Bool
    ) {
        let itemsChanged = !sameItems(self.items, items)
        let qualityChanged = self.qualityName != quality.name

        self.items = items
        self.qualityName = quality.name

        if itemsChanged || qualityChanged {
            self.index = clamp(startIndex)
            let resumeTime = playerViewController.player?.currentTime()
            configureCurrentItem(muted: muted, resumeTime: resumeTime)
        } else {
            playerViewController.player?.isMuted = muted
        }
        updateOverlayControls()
    }

    func teardown() {
        removeEndObserver()
        timeControlObserver?.invalidate()
        timeControlObserver = nil
        playerViewController.player?.pause()
        playerViewController.player = nil
        playerViewController.stopMonitoring()
    }

    @objc private func goToPrevious() {
        select(index - 1)
    }

    @objc private func goToNext() {
        select(index + 1)
    }

    private func select(_ requested: Int) {
        guard items.indices.contains(requested) else { return }
        index = requested
        configureCurrentItem(muted: playerViewController.player?.isMuted ?? false)
        listener?.onIndexChanged(index: Int32(index))
    }

    private func advance() {
        guard index + 1 < items.count else { return }
        select(index + 1)
    }

    private func configureCurrentItem(muted: Bool, resumeTime: CMTime? = nil) {
        guard items.indices.contains(index) else { return }
        let item = items[index]

        removeEndObserver()

        playerViewController.prepare(
            playbackID: item.playbackId,
            playbackOptions: playbackOptions(for: item)
        )

        playerViewController.player?.isMuted = muted
        playerViewController.player?.play()

        if let resumeTime, resumeTime.isNumeric, resumeTime.seconds > 0 {
            playerViewController.player?.seek(to: resumeTime)
        }

        observeEnd()
        observeTimeControlStatus()
        updateOverlayControls()
        preferOfflineIfAvailable(item: item, muted: muted)
    }

    private func preferOfflineIfAvailable(item: VideoItem, muted: Bool) {
        Task { @MainActor in
            guard
                let downloaded = await MuxOfflineAccessManager.shared.findDownloadedAsset(
                    playbackID: item.playbackId
                ),
                let asset = downloaded.avAssetIfPlayable()
            else { return }

            guard items.indices.contains(index), items[index].playbackId == item.playbackId else {
                return
            }

            playerViewController.player?.replaceCurrentItem(
                with: AVPlayerItem(asset: asset)
            )
            playerViewController.player?.isMuted = muted
            playerViewController.player?.play()
            observeEnd()
        }
    }

    private func observeEnd() {
        guard let current = playerViewController.player?.currentItem else { return }
        endObserver = NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: current,
            queue: .main
        ) { [weak self] _ in
            MainActor.assumeIsolated {
                self?.advance()
            }
        }
    }

    private func removeEndObserver() {
        if let endObserver {
            NotificationCenter.default.removeObserver(endObserver)
            self.endObserver = nil
        }
    }

    private func observeTimeControlStatus() {
        timeControlObserver?.invalidate()
        guard let player = playerViewController.player else { return }
        timeControlObserver = player.observe(\.timeControlStatus, options: [.initial, .new]) { [weak self] player, _ in
            let isPlaying = player.timeControlStatus != .paused
            Task { @MainActor in
                self?.listener?.onIsPlayingChanged(isPlaying: isPlaying)
            }
        }
    }

    private func setupTapRecognizer() {
        let tap = UITapGestureRecognizer(target: self, action: #selector(handlePlayerTap))
        tap.cancelsTouchesInView = false
        tap.delegate = self
        view.addGestureRecognizer(tap)
    }

    @objc private func handlePlayerTap() {
        listener?.onControlsVisibilityChanged(visible: true)
    }

    private func clamp(_ requested: Int) -> Int {
        guard !items.isEmpty else { return 0 }
        return max(0, min(requested, items.count - 1))
    }

    private func sameItems(_ lhs: [VideoItem], _ rhs: [VideoItem]) -> Bool {
        guard lhs.count == rhs.count else { return false }
        for (left, right) in zip(lhs, rhs) {
            if left.playbackId != right.playbackId
                || left.token480 != right.token480
                || left.token720 != right.token720
                || left.token1080 != right.token1080 {
                return false
            }
        }
        return true
    }

    private func playbackOptions(for item: VideoItem) -> PlaybackOptions {
        if let token = token(for: item), !token.isEmpty {
            return PlaybackOptions(playbackToken: token)
        }
        switch qualityName {
        case "R1080":
            return PlaybackOptions(maximumResolutionTier: .upTo1080p)
        case "R480":
            return PlaybackOptions(
                maximumResolutionTier: .upTo720p,
                minimumResolutionTier: .atLeast480p
            )
        default:
            return PlaybackOptions(maximumResolutionTier: .upTo720p)
        }
    }

    private func token(for item: VideoItem) -> String? {
        switch qualityName {
        case "R480": return item.token480
        case "R720": return item.token720
        case "R1080": return item.token1080
        default: return nil
        }
    }

    private func setupOverlayControls() {
        guard let overlay = playerViewController.contentOverlayView else { return }

        for button in [previousButton, nextButton] {
            button.translatesAutoresizingMaskIntoConstraints = false
            overlay.addSubview(button)
        }

        NSLayoutConstraint.activate([
            previousButton.leadingAnchor.constraint(equalTo: overlay.leadingAnchor, constant: 24),
            previousButton.centerYAnchor.constraint(equalTo: overlay.centerYAnchor),
            nextButton.trailingAnchor.constraint(equalTo: overlay.trailingAnchor, constant: -24),
            nextButton.centerYAnchor.constraint(equalTo: overlay.centerYAnchor),
        ])

        updateOverlayControls()
    }

    private func updateOverlayControls() {
        let showsNavigation = items.count > 1
        previousButton.isHidden = !showsNavigation
        nextButton.isHidden = !showsNavigation
    }

    private func makeOverlayButton(systemName: String, action: Selector) -> UIButton {
        let configuration = UIImage.SymbolConfiguration(pointSize: 22, weight: .bold)
        let image = UIImage(systemName: systemName, withConfiguration: configuration)
        let button = UIButton(type: .system)
        button.setImage(image, for: .normal)
        button.tintColor = .white
        button.backgroundColor = UIColor.black.withAlphaComponent(0.4)
        button.layer.cornerRadius = 22
        button.translatesAutoresizingMaskIntoConstraints = false
        button.widthAnchor.constraint(equalToConstant: 44).isActive = true
        button.heightAnchor.constraint(equalToConstant: 44).isActive = true
        button.addTarget(self, action: action, for: .touchUpInside)
        return button
    }
}

extension MuxPlayerViewController: UIGestureRecognizerDelegate {
    func gestureRecognizer(
        _ gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer
    ) -> Bool {
        true
    }
}
