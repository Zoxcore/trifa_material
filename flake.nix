{
  description = "Development environment with Skiko support";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";
    # nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11"; # old nixos stable repo
    nixpkgs-unstable.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      nixpkgs-unstable,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        pkgs-unstable = nixpkgs-unstable.legacyPackages.${system};

        jdk = pkgs-unstable.temurin-bin-26;

        # Group all the libraries together
        runtimeLibs = with pkgs; [
          stdenv.cc.cc.lib # <-- 1. This provides libstdc++.so.6
          libGL
          libX11
          libXcursor
          libXrandr
          libXrender
          libXext
          libXxf86vm
          fontconfig
          freetype
          libv4l
          libnotify
          libxcb
          libpulseaudio
          alsa-lib
        ];
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk
          ] ++ runtimeLibs;

          shellHook = ''
            export JAVA_HOME=${jdk}
            export PATH="$JAVA_HOME/bin:$PATH"
            export _JAVA_OPTIONS="-Dskiko.linux.autodpi=false -Dsun.java2d.uiScale=2"
            export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath runtimeLibs}:$LD_LIBRARY_PATH"
            
            # This feeds nix-ld the libraries Skiko tries to dynamically load at runtime
            export NIX_LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath runtimeLibs}"
            
            echo "Java 26 & Skiko environment loaded cleanly via nix-ld!"
          '';
        };
      }
    );
}
