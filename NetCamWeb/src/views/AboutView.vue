<template>
  <div class="about">
    <div>
      <video id="local" width="320" height="240" style="background: black; margin: 5px;" autoplay></video>
      <video id="remote" width="320" height="240" style="background: black; margin: 5px;" autoplay></video>
    </div>
    <div>
      <input type="text" v-model="number" />
      <button @click="onConnectClick">连接</button>
      <input type="text" v-model="remote_number" />
      <button @click="onCallBtnClick">{{ call.text }}</button>
      <button @click="onDisconnectClick">断开连接</button>
    </div>
  </div>
</template>

<script>

let constraints = {
  audio: true,
  video: true
}

export default {
  name: "AboutView",
  data() {
    return {
      number: '1001',
      remote_number: '1002',
      peer: null,
      socket: null,
      call: {
        type: 'invite',
        text: "呼叫"
      }
    }
  },
  mounted() {
    
  },
  methods: {
    onMessage(event) {
      // console.log("message from server: ", event);
      try {
        let data = JSON.parse(event.data);
        if (data.action == 'answer') {
          console.log("answer: ", data);
          this.remote_number = data.from;
          let sdp = new RTCSessionDescription({type: data.type, sdp: data.sdp});
          this.peer.setRemoteDescription(sdp);
          this.call.text = "挂断";
          this.call.type = "oncall";
        } else if (data.action == 'candidate') {
          console.log("candidate: ", data);
          let candidate = new RTCIceCandidate(data.candidate);
          this.peer.addIceCandidate(candidate);
        } else if (data.action == 'invite') {
          this.call.type = 'answer';
          this.call.text = "接听";
          this.remote_number = data.from;
          let rsdp = new RTCSessionDescription({type: data.type, sdp: data.sdp});
          this.peer.setRemoteDescription(rsdp);
        } else if (data.action == 'hangup') {
          this.call.text = "呼叫";
          this.call.type = "invite";
        } else {
          console.log("response: ", data);
        }
      } catch (e) {
        console.error(e);
      }
    },
    sendToCallee(data) {
      console.log("action: ", data.action, data);
      this.socket.send(JSON.stringify(data));
    },
    initPeer() {
      let remote = document.getElementById("remote");
      let remoteStream = new MediaStream();

      remote.srcObject = remoteStream;

      this.peer = new RTCPeerConnection();
      this.peer.onicecandidate = (event) => {
        if (event.candidate) {
          let data = {
            action: 'candidate',
            from: this.number,
            to: this.remote_number,
            candidate: event.candidate
          };
          this.sendToCallee(data);
        }
      }

      this.peer.ontrack = (event) => {
        console.log("ontrack: ", event);
        remoteStream.addTrack(event.track);
      }

      this.peer.oniceconnectionstatechange = (e) => {
        console.log("oniceconnectionstatechange: ", e);
      }

      this.peer.onicecandidateerror = (e) => {
        console.log("onicecandidateerror: ", e);
      }

      this.peer.onicegatheringstatechange = (e) => {
        console.log("onicegatheringstatechange: ", e);
      }
    },
    async initLocalMedia() {
      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      let local = document.getElementById("local");
      local.srcObject = stream;
      this.peer.addStream(stream);
    },
    async onCallClick(e) {
      console.log("on call clicked:", e);
      try {
        const offer = await this.peer.createOffer({offerToReceiveAudio: true, offerToReceiveVideo: true});
        this.peer.setLocalDescription(offer);

        let data = {
          action: 'invite',
          sdp: offer.sdp,
          type: offer.type,
          caller: this.number,
          callee: this.remote_number
        };
        this.sendToCallee(data);
      } catch (error) {
        console.error(error);
      }
    },
    async onAnswerClick(e) {
      console.log("on answer clicked:", e);
      try {
        const answer = await this.peer.createAnswer({offerToReceiveAudio: true, offerToReceiveVideo: true});
        this.peer.setLocalDescription(answer);

        let data = {
          action: 'answer', 
          caller: this.remote_number,
          callee: this.number, 
          sdp: answer.sdp, 
          type: answer.type
        };
        this.sendToCallee(data);
      } catch (error) {
        console.error(error);
      }
    },
    onCallBtnClick(e) {
      if (this.call.type == 'invite') {
        this.call.text = "呼叫中"
        this.onCallClick(e);
      } else if(this.call.type == 'answer') {
        this.call.text = "挂断";
        this.call.type = "oncall";
        this.onAnswerClick(e);
      } else {
        let data = {
          action: 'hangup',
          from: this.number,
          to: this.remote_number
        };
        this.sendToCallee(data);
        this.call.text = "呼叫";
        this.call.type = "invite";
      }
    },
    onConnectClick(e) {
      console.log("on connect clicked:", e);
      this.socket = new WebSocket("ws://localhost:5173/test");

      this.socket.addEventListener('open', (event) => {
        console.log("open socket: ", event);
        /* 注册信息 */
        this.sendToCallee({
          action: 'register',
          number: this.number,
        })
      });

      this.socket.addEventListener('message', this.onMessage);

      this.initPeer();
      this.initLocalMedia();
    },
    onDisconnectClick(e) {
      console.log("on disconnect clicked:", e);
      this.sendToCallee({action: "unregister", number: this.number});
      this.peer.close();
      this.socket.close();
      this.call.text = "呼叫";
      this.call.type = "invite";
    }
  }

}

</script>

<style>
@media (min-width: 1024px) {
  .about {
    min-height: 100vh;
    display: flex;
    align-items: center;
  }
}
</style>
