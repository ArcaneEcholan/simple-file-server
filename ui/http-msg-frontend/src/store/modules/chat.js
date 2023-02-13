
const state = {
    onlineUsers: [],
    chats: [],
    curChat: {},
    me: {}
}

const mutations = {
    TOGGLE_DEVICE: (state, device) => {
        state.device = device
    }
}

const actions = {

    closeSideBar({ commit }, { withoutAnimation }) {
        commit('CLOSE_SIDEBAR', withoutAnimation)
    }

}

export default {
    namespaced: true,
    state,
    mutations,
    actions
}
