export const manifest = {
  screens: {
    scr_1lfhgh: { name: "Landing", route: "/", position: { "x": 160, "y": 220 } },
    scr_rne5t4: { name: "Login", route: "/login", position: { "x": 1560, "y": 220 } },
    scr_4b8r1n: { name: "Register", route: "/register", position: { "x": 2960, "y": 220 } },
    scr_35klqv: { name: "Dashboard", route: "/dashboard", position: { "x": 160, "y": 10120 } },
    scr_x2psuf: { name: "Resume Upload", route: "/resume", position: { "x": 160, "y": 2200 } },
    scr_rty0ls: { name: "Choose Company", route: "/interview/company", position: { "x": 160, "y": 4180 } },
    scr_wycecz: { name: "Choose Role", route: "/interview/role", position: { "x": 1560, "y": 4180 } },
    scr_okcqcs: { name: "Interview Config", route: "/interview/config", position: { "x": 2960, "y": 4180 } },
    scr_c0d4yt: { name: "Interview Room", route: "/interview/room", position: { "x": 160, "y": 6160 } },
    scr_sz90m9: { name: "AI Evaluation", route: "/evaluation", position: { "x": 160, "y": 8140 } },
    scr_twq01r: { name: "Skill Gap Analysis", route: "/skill-gap", position: { "x": 1560, "y": 8140 } },
    scr_fxby3y: { name: "Learning Roadmap", route: "/roadmap", position: { "x": 2960, "y": 8140 } },
    scr_tkplel: { name: "Interview History", route: "/history", position: { "x": 1560, "y": 10120 } },
    scr_wbm3ds: { name: "Analytics", route: "/analytics", position: { "x": 1560, "y": 12100 } },
    scr_5kgodg: { name: "Profile", route: "/profile", position: { "x": 160, "y": 12100 } },
    scr_za4qfh: { name: "Admin Dashboard", route: "/admin", position: { "x": 160, "y": 14080 } }
  },
  sections: {
    sec_ow573k: { name: "Authentication", x: 0, y: 0, width: 4320, height: 1180 },
    sec_i5rriu: { name: "Resume Setup", x: 0, y: 1980, width: 1520, height: 1180 },
    sec_p6t57b: { name: "Interview Setup", x: 0, y: 3960, width: 4320, height: 1180 },
    sec_ub2y3u: { name: "Interview Execution", x: 0, y: 5940, width: 1520, height: 1180 },
    sec_157lce: { name: "Post-Interview Analysis", x: 0, y: 7920, width: 4320, height: 1180 },
    sec_25f7dv: { name: "Dashboard & History", x: 0, y: 9900, width: 2920, height: 1180 },
    sec_vmwvrx: { name: "User Settings", x: 0, y: 11880, width: 2920, height: 1180 },
    sec_cax4ee: { name: "Admin", x: 0, y: 13860, width: 1520, height: 1180 }
  },
  layers: [
  { kind: "section", id: "sec_ow573k", children: [
    { kind: "screen", id: "scr_1lfhgh" },
    { kind: "screen", id: "scr_rne5t4" },
    { kind: "screen", id: "scr_4b8r1n" }]
  },
  { kind: "section", id: "sec_i5rriu", children: [
    { kind: "screen", id: "scr_x2psuf" }]
  },
  { kind: "section", id: "sec_p6t57b", children: [
    { kind: "screen", id: "scr_rty0ls" },
    { kind: "screen", id: "scr_wycecz" },
    { kind: "screen", id: "scr_okcqcs" }]
  },
  { kind: "section", id: "sec_ub2y3u", children: [
    { kind: "screen", id: "scr_c0d4yt" }]
  },
  { kind: "section", id: "sec_157lce", children: [
    { kind: "screen", id: "scr_sz90m9" },
    { kind: "screen", id: "scr_twq01r" },
    { kind: "screen", id: "scr_fxby3y" }]
  },
  { kind: "section", id: "sec_25f7dv", children: [
    { kind: "screen", id: "scr_35klqv" },
    { kind: "screen", id: "scr_tkplel" }]
  },
  { kind: "section", id: "sec_vmwvrx", children: [
    { kind: "screen", id: "scr_5kgodg" },
    { kind: "screen", id: "scr_wbm3ds" }]
  },
  { kind: "section", id: "sec_cax4ee", children: [
    { kind: "screen", id: "scr_za4qfh" }]
  }]

};