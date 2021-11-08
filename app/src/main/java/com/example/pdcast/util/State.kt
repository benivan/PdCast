package com.example.pdcast.util

enum class PState(val value: Int)  {
     STATE_NONE(0),
     STATE_STOPPED(1),

    /**
     * State indicating this item is currently paused.
     *
     * @see Builder.setState
     */
     STATE_PAUSED(2),

    /**
     * State indicating this item is currently playing.
     *
     * @see Builder.setState
     */
     STATE_PLAYING(3),

    /**
     * State indicating this item is currently fast forwarding.
     *
     * @see Builder.setState
     */
     STATE_FAST_FORWARDING(4),

    /**
     * State indicating this item is currently rewinding.
     *
     * @see Builder.setState
     */
     STATE_REWINDING(5),

    /**
     * State indicating this item is currently buffering and will begin playing
     * when enough data has buffered.
     *
     * @see Builder.setState
     */
     STATE_BUFFERING(6),

    /**
     * State indicating this item is currently in an error state. The error
     * code should also be set when entering this state.
     *
     * @see Builder.setState
     *
     * @see Builder.setErrorMessage
     */
     STATE_ERROR(7),

    /**
     * State indicating the class doing playback is currently connecting to a
     * route. Depending on the implementation you may return to the previous
     * state when the connection finishes or enter [.STATE_NONE]. If
     * the connection failed [.STATE_ERROR] should be used.
     *
     *
     * On devices earlier than API d)1, this will appear as [.STATE_BUFFERING]
     *
     *
     * @see Builder.setState
     */
     STATE_CONNECTING(8),

    /**
     * State indicating the player is currently skipping to the previous item.
     *
     * @see Builder.setState
     */
     STATE_SKIPPING_TO_PREVIOUS(9),

    /**
     * State indicating the player is currently skipping to the next item.
     *
     * @see Builder.setState
     */
     STATE_SKIPPING_TO_NEXT(10),

    /**
     * State indicating the player is currently skipping to a specific item in
     * the queue.
     *
     *
     * On devices earlier than API 21, this will appear as [.STATE_SKIPPING_TO_NEXT]
     *
     *
     * @see Builder.setState
     */
     STATE_SKIPPING_TO_QUEUE_ITEM(11);

    companion object {
        fun of(value: Int) = enumValues<PState>().first { it.value == value }
    }
}


