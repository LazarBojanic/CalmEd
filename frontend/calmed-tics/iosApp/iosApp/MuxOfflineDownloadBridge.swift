import Foundation
import MuxPlayerSwift
import Shared

final class MuxOfflineDownloadBridge: NSObject, IosOfflineDownloadBridge {

    private weak var listener: IosOfflineDownloadListener?
    private var tasks: [String: Task<Void, Never>] = [:]

    func startObserving(listener: IosOfflineDownloadListener) {
        self.listener = listener
    }

    func resumePendingDownloads() {
        MuxOfflineAccessManager.shared.resumePendingDownloadTasks()
        refresh()
    }

    func refresh() {
        Task { @MainActor in
            await publishDownloadedAssets()

            let inProgress = await MuxOfflineAccessManager.shared.allInProcessTasks()
            for (playbackID, stream) in inProgress {
                observe(playbackId: playbackID, stream: stream)
            }
        }
    }

    func startDownload(
        playbackId: String,
        token: String?,
        title: String?,
        maxResolution: String
    ) {
        let playbackOptions: PlaybackOptions
        if let token, !token.isEmpty {
            playbackOptions = PlaybackOptions(playbackToken: token)
        } else {
            playbackOptions = PlaybackOptions(
                maximumResolutionTier: maxTier(for: maxResolution)
            )
        }

        let downloadOptions = DownloadOptions(readableTitle: title ?? playbackId)

        listener?.onDownloadState(
            playbackId: playbackId,
            status: "Starting",
            progress: 0,
            title: title
        )

        Task { @MainActor in
            let stream = await MuxOfflineAccessManager.shared.startDownload(
                playbackID: playbackId,
                playbackOptions: playbackOptions,
                downloadOptions: downloadOptions
            )
            observe(playbackId: playbackId, stream: stream)
        }
    }

    func removeDownload(playbackId: String) {
        tasks[playbackId]?.cancel()
        tasks[playbackId] = nil

        Task { @MainActor in
            await MuxOfflineAccessManager.shared.removeDownload(playbackID: playbackId)
            listener?.onDownloadRemoved(playbackId: playbackId)
        }
    }

    @MainActor
    private func publishDownloadedAssets() async {
        let assets = await MuxOfflineAccessManager.shared.allDownloadedAssets()
        for asset in assets {
            let title = asset.downloadOptions.readableTitle
            switch asset.assetStatus {
            case .playable:
                listener?.onDownloadState(
                    playbackId: asset.playbackID,
                    status: "Downloaded",
                    progress: 100,
                    title: title
                )
            case .expired:
                listener?.onDownloadState(
                    playbackId: asset.playbackID,
                    status: "Expired",
                    progress: -1,
                    title: title
                )
            case .redownloadWhenOnline:
                listener?.onDownloadState(
                    playbackId: asset.playbackID,
                    status: "Failed",
                    progress: -1,
                    title: title
                )
            default:
                listener?.onDownloadState(
                    playbackId: asset.playbackID,
                    status: "Failed",
                    progress: -1,
                    title: title
                )
            }
        }
    }

    private func observe(
        playbackId: String,
        stream: AsyncThrowingStream<DownloadEvent, Error>
    ) {
        tasks[playbackId]?.cancel()
        tasks[playbackId] = Task { @MainActor in
            do {
                for try await event in stream {
                    if Task.isCancelled { return }
                    switch event {
                    case .started:
                        listener?.onDownloadState(
                            playbackId: playbackId,
                            status: "Downloading",
                            progress: 0,
                            title: nil
                        )
                    case .waitingForConnectivity:
                        listener?.onDownloadState(
                            playbackId: playbackId,
                            status: "Queued",
                            progress: -1,
                            title: nil
                        )
                    case .progress(let percent):
                        listener?.onDownloadState(
                            playbackId: playbackId,
                            status: "Downloading",
                            progress: percent,
                            title: nil
                        )
                    case .completed(let asset):
                        let title = asset.downloadOptions.readableTitle
                        if asset.avAssetIfPlayable() != nil {
                            listener?.onDownloadState(
                                playbackId: playbackId,
                                status: "Downloaded",
                                progress: 100,
                                title: title
                            )
                        } else {
                            listener?.onDownloadState(
                                playbackId: playbackId,
                                status: "Failed",
                                progress: -1,
                                title: title
                            )
                        }
                    }
                }
            } catch {
                if !Task.isCancelled {
                    listener?.onDownloadState(
                        playbackId: playbackId,
                        status: "Failed",
                        progress: -1,
                        title: nil
                    )
                }
            }
            tasks[playbackId] = nil
        }
    }

    private func maxTier(for resolution: String) -> MaxResolutionTier {
        switch resolution {
        case "1080p": return .upTo1080p
        case "1440p": return .upTo1440p
        case "2160p": return .upTo2160p
        default: return .upTo720p
        }
    }
}
